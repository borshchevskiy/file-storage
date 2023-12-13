async function formSubmitFetch(event) {
    event.preventDefault();

    const form = event.currentTarget;
    // const url = new URL(form.action);
    // const formData = new FormData(form);
    // formData.set('path', decodeURIComponent(formData.get('path')));
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