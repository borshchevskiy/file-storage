const token = $("meta[name='_csrf']").attr('content');
const header = $("meta[name='_csrf_header']").attr('content');

const filesInputForm = document.getElementById('filesInputForm');
const filesInputButton = document.getElementById('filesInputSubmit');
const filesInput = document.getElementById('filesInput');
const filesInputLabel = document.getElementById('filesInputLabel');
const filesInputLabelText = document.getElementById('filesInputLabelText');
const filesSpinner = document.getElementById('filesSpinner');

const dirInputForm = document.getElementById('dirInputForm');
const dirInputButton = document.getElementById('dirInputSubmit');
const dirInput = document.getElementById('dirInput');
const dirInputLabel = document.getElementById('dirInputLabel');
const dirInputLabelText = document.getElementById('dirInputLabelText');
const dirSpinner = document.getElementById('dirSpinner');

const filesList = document.getElementById('filesList');

dirInputForm.addEventListener('submit', handleDirSubmit)
filesInputForm.addEventListener('submit', handleFilesSubmit)

dirInput.addEventListener('input', showDirFileNames)
filesInput.addEventListener('input', showFileNames)

function showDirFileNames(event) {
    let listing = document.getElementById("listing");
    listing.innerHTML = "";
    const theFiles = event.target.files;

    if (theFiles.length === 0) {
        dirInputLabelText.innerHTML = 'Select folder';
        listing.innerHTML = "Folder is empty!";
    }

    for (const file of theFiles) {
        let item = document.createElement("li");
        item.textContent = file.webkitRelativePath;
        listing.appendChild(item);
    }

    const relativePath = theFiles[0].webkitRelativePath;
    const folder = relativePath.split("/");
    dirInputLabelText.innerHTML = 'Folder selected: ' + folder[0];
    dirInputLabel.classList.add("mb-3");
}

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

function removeDirNames() {
    dirInputLabelText.innerHTML = 'Select folder';
    document.getElementById("listing").innerHTML = "";
    dirInputLabel.classList.remove("mb-3");
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

function disableDirInputElements() {
    dirInputButton.setAttribute('value', 'Folder uploading...');
    dirInputButton.setAttribute('disabled', '');
    dirInput.setAttribute('hidden', '');
    dirInputLabel.setAttribute('hidden', '');
    dirSpinner.removeAttribute('hidden');
}

function enableDirInputElements() {
    dirInputButton.setAttribute('value', 'Upload folder');
    dirInputButton.removeAttribute('disabled');
    dirInput.removeAttribute('hidden');
    dirInputLabel.removeAttribute('hidden');
    dirSpinner.setAttribute('hidden', '');
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

    if (response.redirected) {
        let redirectResponse = await fetch(response.url);
        filesList.innerHTML = await redirectResponse.text();
    } else {
        document.body.innerHTML = await response.text();
    }
    let deleteForm = document.getElementById('deleteForm');
    deleteForm.addEventListener('submit', handleDelete);
}

async function handleDirSubmit(event) {
    event.preventDefault();
    disableDirInputElements();

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

    removeDirNames();
    enableDirInputElements();

    if (response.redirected) {
        let redirectResponse = await fetch(response.url);
        filesList.innerHTML = await redirectResponse.text();
    } else {
        document.body.innerHTML = await response.text();
    }

    let deleteForm = document.getElementById('deleteForm');
    deleteForm.addEventListener('submit', handleDelete);
}