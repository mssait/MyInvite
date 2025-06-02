import PROXY from "./proxy"

const fetcher = (url, options) => {
    const update = {...options}
    update.headers = {
        ...update.headers
    }
    update.credentials = 'include'
    update.mode = 'cors'
    return fetch(PROXY + url, update)
}
export default fetcher;