<template>
    <div class="h-screen overflow-y-auto bg-gradient-to-br from-blue-600 to-indigo-100 font-sans">
      <div class="container mx-auto px-4 py-6 max-w-7xl">
        <!-- Header -->
        <HeaderComponent
            title="Gestão de Administradores"
            subtitle="Registre novos administradores ou altere sua senha"
            back-to="/admin"
            :show-user-button="false"
        />  
        <!-- Componente para registrar novos admins -->
        <RegisterAdmin />
  
        <!-- Componente para alterar a própria senha -->
        <ChangePassword />
      </div>
    </div>
  </template>
  
  <script>
  import HeaderComponent from "@/components/Static/HeaderComponent.vue"
  import RegisterAdmin from "@/components/User/RegisterAdmin.vue"
  import ChangePassword from "@/components/User/ChangePassword.vue"
  
  export default {
    name: "UserManagement",
    components: {
      HeaderComponent,
      RegisterAdmin,
      ChangePassword,
    }
  }
  </script>
  