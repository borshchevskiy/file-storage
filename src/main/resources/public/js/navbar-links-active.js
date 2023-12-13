const currentPath = window.location.pathname;
console.log(currentPath)
let myClass;
if (currentPath.startsWith("/search")) {
    myClass = document.getElementById("searchLink");
    myClass.setAttribute("class", "nav-link active");
} else if (currentPath.startsWith("/")) {
    myClass = document.getElementById("myFilesLink");
    myClass.setAttribute("class", "nav-link active");
}