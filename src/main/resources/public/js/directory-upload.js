// const token = $("meta[name='_csrf']").attr('content');
// const header = $("meta[name='_csrf_header']").attr('content');

const dirInputForm = document.getElementById('dirInputForm');
const dirInputButton = document.getElementById('dirInputSubmit');
const dirInput = document.getElementById('dirInput');
const dirInputLabel = document.getElementById('dirInputLabel');
const dirInputLabelText = document.getElementById('dirInputLabelText');
const dirSpinner = document.getElementById('dirSpinner');

// const filesList = document.getElementById('filesList');

dirInputForm.addEventListener('submit', handleDirSubmit)

dirInput.addEventListener('input', showDirFileNames)

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

function removeDirNames() {
    dirInputLabelText.innerHTML = 'Select folder';
    document.getElementById("listing").innerHTML = "";
    dirInputLabel.classList.remove("mb-3");
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

    try {
        let response = await fetch(url, fetchOptions);
        await handleFormSubmitResponse(response);
    } catch (error) {
        const messagePlaceholder = document.getElementById('dirUploadResultMessagePlaceholder');
        const wrapper = document.createElement('div');
        wrapper.innerHTML = [
            '<div class="alert alert-danger alert-dismissible" role="alert">' +
            '   <div>"Error while uploading one of the files. You might be exceeding file size limit</div>' +
            '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
            '</div>'
        ].join('');
        messagePlaceholder.append(wrapper);
    }
    removeDirNames();
    enableDirInputElements();
    dirInputForm.reset();
}