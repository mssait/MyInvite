const REGEX = {
    AADHAAR: /^[2-9]\d{3}\s\d{4}\s\d{4}$/,
    PAN: /[A-Z]{5}\d{4}[A-Z]/,
    IFSC: /^[A-Z]{4}0[A-Z\d]{6}$/,
    EMAIL_PHONE: /^(?:\d{10}|\w+@\w+\.\w{2,3})$/,
    ALPHANUMERIC: /[a-zA-Z0-9]/,
    GSTIN: /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/
}
export default REGEX