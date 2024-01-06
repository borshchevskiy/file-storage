const token = $("meta[name='_csrf']").attr('content');
const header = $("meta[name='_csrf_header']").attr('content');

document.addEventListener("DOMContentLoaded", function() {
    updateListenersForDeleteForms();
});