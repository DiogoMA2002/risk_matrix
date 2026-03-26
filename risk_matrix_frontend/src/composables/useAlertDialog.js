import { ref } from 'vue';

export function useAlertDialog() {
  const showAlert = ref(false);
  const alertTitle = ref('');
  const alertMessage = ref('');
  const alertType = ref('info');
  const alertResolve = ref(null);

  function showAlertDialog(title, message, type = 'info') {
    alertTitle.value = title;
    alertMessage.value = message;
    alertType.value = type;
    showAlert.value = true;
    return new Promise((resolve) => {
      alertResolve.value = resolve;
    });
  }

  function handleAlertConfirm() {
    showAlert.value = false;
    if (alertResolve.value) {
      alertResolve.value(true);
      alertResolve.value = null;
    }
  }

  function handleAlertCancel() {
    showAlert.value = false;
    if (alertResolve.value) {
      alertResolve.value(false);
      alertResolve.value = null;
    }
  }

  return {
    showAlert,
    alertTitle,
    alertMessage,
    alertType,
    showAlertDialog,
    handleAlertConfirm,
    handleAlertCancel,
  };
}
