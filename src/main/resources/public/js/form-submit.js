async function formSubmit(event) {
    event.preventDefault();

    const form = event.currentTarget;
    const formDataParams =  new URLSearchParams(new FormData(form));

    formDataParams.set('path', decodeURIComponent(formDataParams.get('path')))

    const fetchOptions = {
        method: form.method,
        body: formDataParams,
        headers: {
            "Content-Type":"application/x-www-form-urlencoded;charset=UTF-8"
        }
    };

    return fetch(form.action, fetchOptions);
}