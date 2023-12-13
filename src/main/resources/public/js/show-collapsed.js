const uploadFileCollapse = document.getElementById('uploadFileCollapse');
const uploadDirectoryCollapse = document.getElementById('uploadDirectoryCollapse');
const createDirectoryCollapse = document.getElementById('createDirectoryCollapse');

uploadFileCollapse.addEventListener('show.bs.collapse', addMargins);
uploadFileCollapse.addEventListener('hide.bs.collapse', removeMargins);

uploadDirectoryCollapse.addEventListener('show.bs.collapse', addMargins);
uploadDirectoryCollapse.addEventListener('hide.bs.collapse', removeMargins);

createDirectoryCollapse.addEventListener('show.bs.collapse', addMargins);
createDirectoryCollapse.addEventListener('hide.bs.collapse', removeMargins);

async function addMargins(event) {
    event.target.parentElement.classList.add("mt-3");
    event.target.parentElement.classList.add("mb-3");
}

async function removeMargins(event) {
    event.target.parentElement.classList.remove("mt-3");
    event.target.parentElement.classList.remove("mb-3");
}