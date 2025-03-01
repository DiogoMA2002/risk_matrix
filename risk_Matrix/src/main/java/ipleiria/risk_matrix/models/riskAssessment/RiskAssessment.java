package ipleiria.risk_matrix.models.riskAssessment;
import ipleiria.risk_matrix.models.assets.Asset;
import ipleiria.risk_matrix.models.threats.Threat;
import ipleiria.risk_matrix.models.vulnerability.Vulnerability;
import jakarta.persistence.*;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "threat_id", nullable = false)
    private Threat threat;

    @ManyToOne
    @JoinColumn(name = "vulnerability_id")
    private Vulnerability vulnerability;

    @Enumerated(EnumType.STRING)
    private RiskLevel probability;

    @Enumerated(EnumType.STRING)
    private RiskLevel impact;

    // Constructors
    public RiskAssessment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public Threat getThreat() { return threat; }
    public void setThreat(Threat threat) { this.threat = threat; }

    public Vulnerability getVulnerability() { return vulnerability; }
    public void setVulnerability(Vulnerability vulnerability) { this.vulnerability = vulnerability; }

    public RiskLevel getProbability() { return probability; }
    public void setProbability(RiskLevel probability) { this.probability = probability; }

    public RiskLevel getImpact() { return impact; }
    public void setImpact(RiskLevel impact) { this.impact = impact; }
}
