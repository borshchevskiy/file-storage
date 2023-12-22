const token = $("meta[name='_csrf']").attr('content');
const header = $("meta[name='_csrf_header']").attr('content');

const filesInputForm = document.getElementById('filesInputForm');
const filesInputButton = document.getElementById('filesInputSubmit');
const filesInput = document.getElementById('filesInput');
const filesInputLabel = document.getElementById('filesInputLabel');
const filesInputLabelText = document.getElementById('filesInputLabelText');
const filesSpinner = document.getElementById('filesSpinner');

const filesList = document.getElementById('filesList');

filesInputForm.addEventListener('submit', handleFilesSubmit)

filesInput.addEventListener('input', showFileNames)

function showFileNames() {
    const fileList = this.files;
    if (fileList.length === 0) {
        return;
    }
    if (fileList.length === 1) {
        filesInputLabelText.innerHTML = fileList.item(0).name;
        return;
    }
    if (fileList.length > 1) {
        filesInputLabelText.innerHTML = 'Selected ' + fileList.length + ' files';
    }
}

function removeFileNames() {
    filesInputLabelText.innerHTML = 'Select file(s)';
}

function disableFilesInputElements() {
    filesInputButton.setAttribute('value', 'File(s) uploading...');
    filesInputButton.setAttribute('disabled', '');
    filesInput.setAttribute('hidden', '');
    filesInputLabel.setAttribute('hidden', '');
    filesSpinner.removeAttribute('hidden');
}

function enableFilesInputElements() {
    filesInputButton.setAttribute('value', 'Upload file(s)');
    filesInputButton.removeAttribute('disabled');
    filesInput.removeAttribute('hidden');
    filesInputLabel.removeAttribute('hidden');
    filesSpinner.setAttribute('hidden', '');
}

async function handleFilesSubmit(event) {
    event.preventDefault();
    disableFilesInputElements();

    const form = event.currentTarget;
    const url = new URL(form.action);
    const formData = new FormData(form);
    const headers = new Headers();

    headers.set(header, token);

    const fetchOptions = {
        method: form.method,
        body: formData,
        headers: headers,
        credentials: 'include',
    };

    let response = await fetch(url, fetchOptions);

    removeFileNames();
    enableFilesInputElements();

    await handleFormSubmitResponse(response);

    let deleteForm = document.getElementById('deleteForm');
    deleteForm.addEventListener('submit', handleDelete);
}