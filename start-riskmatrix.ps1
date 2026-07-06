# ============================================================
#  Risk Matrix - control-window launcher
#  Starts backend (8080) + frontend (8081) as CHILD processes
#  bound to a Windows Job Object. When THIS window closes,
#  Windows kills both children automatically (incl. the JVM
#  that Maven forks). No orphan processes left behind.
# ============================================================

$ErrorActionPreference = 'Stop'
$root     = $PSScriptRoot
$backend  = Join-Path $root 'risk_Matrix'
$frontend = Join-Path $root 'risk_matrix_frontend'
$logs     = Join-Path $root 'logs'

$Host.UI.RawUI.WindowTitle = 'Risk Matrix - Control Window (close to shut down)'

function Fail($msg) {
    Write-Host ""
    Write-Host "[ERROR] $msg" -ForegroundColor Red
    Write-Host ""
    Read-Host "Press Enter to close"
    exit 1
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   Risk Matrix - Startup" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# ---------- 1. Prerequisite checks ----------
# Locate a JDK (>= 23) even when it is not on the system PATH.
# Order: PATH -> JAVA_HOME -> common install dirs (incl. IntelliJ's .jdks).
function Get-JdkMajor($jdkHome) {
    $rel = Join-Path $jdkHome 'release'
    if (Test-Path $rel) {
        $m = (Get-Content $rel | Where-Object { $_ -match '^JAVA_VERSION=' })
        if ($m -match 'JAVA_VERSION="?(\d+)') { return [int]$Matches[1] }
    }
    return 0
}
function Find-Jdk {
    # 1) java already on PATH
    $onPath = Get-Command java -ErrorAction SilentlyContinue
    if ($onPath) { return (Split-Path (Split-Path $onPath.Source -Parent) -Parent) }
    # 2) JAVA_HOME
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { return $env:JAVA_HOME }
    # 3) Search common roots, prefer the highest version >= 23
    $roots = @(
        "$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Microsoft", "$env:ProgramFiles\Amazon Corretto",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium", "$env:USERPROFILE\.jdks"
    )
    # Prefer JDK 23 (the version this project targets); otherwise the
    # lowest JDK newer than 23 that we can find.
    $exact = $null; $fallback = $null; $fallbackMajor = [int]::MaxValue
    foreach ($r in $roots) {
        if (-not (Test-Path $r)) { continue }
        foreach ($d in Get-ChildItem $r -Directory -ErrorAction SilentlyContinue) {
            if (-not (Test-Path (Join-Path $d.FullName 'bin\java.exe'))) { continue }
            $maj = Get-JdkMajor $d.FullName
            if ($maj -eq 23) { $exact = $d.FullName }
            elseif ($maj -gt 23 -and $maj -lt $fallbackMajor) { $fallback = $d.FullName; $fallbackMajor = $maj }
        }
    }
    if ($exact) { return $exact }
    return $fallback
}

$jdk = Find-Jdk
if (-not $jdk) {
    Fail "No JDK 23+ found. Install JDK 23 (Temurin): https://adoptium.net/temurin/releases/?version=23  then run this again."
}
$env:JAVA_HOME = $jdk
$env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH
$jdkMajor = Get-JdkMajor $jdk

$node = Get-Command node -ErrorAction SilentlyContinue
if (-not $node) {
    Fail "Node.js not found. Install Node 18+ (LTS): https://nodejs.org/en/download  then run this again."
}
Write-Host "[OK] Java $jdkMajor  ($jdk)"
Write-Host "[OK] Node found  ($($node.Source))"

# ---------- 2. Ensure backend .env ----------
$envFile = Join-Path $backend '.env'
if (-not (Test-Path $envFile)) {
    $example = Join-Path $backend '.env.example'
    if (-not (Test-Path $example)) { Fail "No .env or .env.example found in $backend" }
    Copy-Item $example $envFile
    Write-Host "[OK] Created backend .env from .env.example"
} else {
    Write-Host "[OK] Backend .env present"
}

# Read admin credentials from .env so we can show them
$adminUser = 'admin'
$adminPass = '(see risk_Matrix\.env)'
foreach ($line in Get-Content $envFile) {
    if ($line -match '^\s*ADMIN_USERNAME\s*=\s*(.+?)\s*$') { $adminUser = $Matches[1] }
    if ($line -match '^\s*ADMIN_PASSWORD\s*=\s*(.+?)\s*$') { $adminPass = $Matches[1] }
}

# ---------- 3. Frontend dependencies ----------
if (-not (Test-Path (Join-Path $frontend 'node_modules'))) {
    Write-Host ""
    Write-Host "Installing frontend dependencies (first run only, may take a few minutes)..." -ForegroundColor Yellow
    Push-Location $frontend
    cmd /c "npm install"
    $code = $LASTEXITCODE
    Pop-Location
    if ($code -ne 0) { Fail "npm install failed (see output above)." }
} else {
    Write-Host "[OK] Frontend dependencies installed"
}

New-Item -ItemType Directory -Force -Path $logs | Out-Null

# ---------- 4. Job Object: children die when this window closes ----------
$jobType = @'
using System;
using System.Runtime.InteropServices;
public static class ChildJob {
    [StructLayout(LayoutKind.Sequential)]
    struct IO_COUNTERS { public ulong a,b,c,d,e,f; }
    [StructLayout(LayoutKind.Sequential)]
    struct JOBOBJECT_BASIC_LIMIT_INFORMATION {
        public long PerProcessUserTimeLimit; public long PerJobUserTimeLimit;
        public uint LimitFlags; public UIntPtr MinimumWorkingSetSize; public UIntPtr MaximumWorkingSetSize;
        public uint ActiveProcessLimit; public UIntPtr Affinity; public uint PriorityClass; public uint SchedulingClass;
    }
    [StructLayout(LayoutKind.Sequential)]
    struct JOBOBJECT_EXTENDED_LIMIT_INFORMATION {
        public JOBOBJECT_BASIC_LIMIT_INFORMATION BasicLimitInformation;
        public IO_COUNTERS IoInfo; public UIntPtr ProcessMemoryLimit; public UIntPtr JobMemoryLimit;
        public UIntPtr PeakProcessMemoryUsed; public UIntPtr PeakJobMemoryUsed;
    }
    [DllImport("kernel32.dll", CharSet=CharSet.Unicode)] static extern IntPtr CreateJobObject(IntPtr a, string name);
    [DllImport("kernel32.dll")] static extern bool SetInformationJobObject(IntPtr h, int c, IntPtr info, uint len);
    [DllImport("kernel32.dll", SetLastError=true)] static extern bool AssignProcessToJobObject(IntPtr job, IntPtr proc);
    static IntPtr hJob = IntPtr.Zero;
    public static void Init() {
        hJob = CreateJobObject(IntPtr.Zero, null);
        var ext = new JOBOBJECT_EXTENDED_LIMIT_INFORMATION();
        ext.BasicLimitInformation.LimitFlags = 0x2000; // KILL_ON_JOB_CLOSE
        int len = Marshal.SizeOf(ext);
        IntPtr p = Marshal.AllocHGlobal(len);
        Marshal.StructureToPtr(ext, p, false);
        SetInformationJobObject(hJob, 9, p, (uint)len); // ExtendedLimitInformation
        Marshal.FreeHGlobal(p);
    }
    public static void Add(IntPtr proc) { AssignProcessToJobObject(hJob, proc); }
}
'@
Add-Type -TypeDefinition $jobType
[ChildJob]::Init()

function Start-Child($file, $procArgs, $workdir, $log) {
    $p = Start-Process -FilePath $file -ArgumentList $procArgs -WorkingDirectory $workdir `
         -WindowStyle Hidden -PassThru `
         -RedirectStandardOutput $log -RedirectStandardError "$log.err"
    [ChildJob]::Add($p.Handle)
    return $p
}

# ---------- 5. Start backend ----------
Write-Host ""
Write-Host "Starting backend (http://localhost:8080) ..." -ForegroundColor Yellow
$mvnw = Join-Path $backend 'mvnw.cmd'
$be = Start-Child 'cmd.exe' "/c `"$mvnw`" spring-boot:run" $backend (Join-Path $logs 'backend.log')

# ---------- 6. Wait for backend port 8080 ----------
Write-Host "Waiting for backend to come up (30-60s is normal)..." -ForegroundColor Yellow
$up = $false
for ($i = 0; $i -lt 90; $i++) {
    if ($be.HasExited) { Fail "Backend stopped during startup. See logs\backend.log" }
    try {
        $c = New-Object Net.Sockets.TcpClient
        $c.Connect('127.0.0.1', 8080); $c.Close(); $up = $true; break
    } catch { Start-Sleep -Seconds 2 }
}
if ($up) { Write-Host "[OK] Backend is up." -ForegroundColor Green }
else     { Write-Host "[WARN] Backend not responding yet; starting frontend anyway. Check logs\backend.log" -ForegroundColor Yellow }

# ---------- 7. Start frontend ----------
Write-Host "Starting frontend (http://localhost:8081) ..." -ForegroundColor Yellow
$fe = Start-Child 'cmd.exe' '/c npm run serve' $frontend (Join-Path $logs 'frontend.log')

# ---------- 8. Open browser ----------
Start-Sleep -Seconds 12
Start-Process 'http://localhost:8081'

# ---------- 9. Control banner + keep window alive ----------
Clear-Host
Write-Host ""
Write-Host "  ================================================" -ForegroundColor Green
Write-Host "   Risk Matrix is RUNNING" -ForegroundColor Green
Write-Host "  ================================================" -ForegroundColor Green
Write-Host ""
Write-Host "   Application : " -NoNewline; Write-Host "http://localhost:8081" -ForegroundColor Cyan
Write-Host "   API / Docs  : " -NoNewline; Write-Host "http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "   Admin user  : " -NoNewline; Write-Host $adminUser -ForegroundColor White
Write-Host "   Admin pass  : " -NoNewline; Write-Host $adminPass -ForegroundColor White
Write-Host ""
Write-Host "   Logs        : logs\backend.log , logs\frontend.log"
Write-Host ""
Write-Host "  ------------------------------------------------" -ForegroundColor DarkGray
Write-Host "   CLOSE THIS WINDOW to shut the application down." -ForegroundColor Yellow
Write-Host "   (or press Ctrl+C here)" -ForegroundColor DarkGray
Write-Host "  ================================================" -ForegroundColor Green
Write-Host ""

try {
    # Keep the control window open. If either child dies, report and wait.
    while (-not $be.HasExited -and -not $fe.HasExited) { Start-Sleep -Seconds 1 }
    Write-Host ""
    Write-Host "[!] A component stopped. Check the logs folder." -ForegroundColor Red
    Read-Host "Press Enter to shut down and close"
}
finally {
    # Belt-and-suspenders: also kill on Ctrl+C / normal exit.
    foreach ($p in @($be, $fe)) {
        if ($p -and -not $p.HasExited) { try { $p.Kill() } catch {} }
    }
}
