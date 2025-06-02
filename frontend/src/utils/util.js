import Decimal from "decimal.js";
import moment from "moment/moment";

export const toImage = id => `https://download-accl.zoho.in/public/workdrive/previewdata/${id}?orig=true`
export const WorkDriveImage = ({ image, alt, auto, width = '100%' }) => (
    <img
        style={auto === 'width' ? { width: 'auto', height: '100%' } : { width, height: 'auto' }}
        src={toImage(image)}
        alt={alt}
    />
)
export const toDate = time => time ? moment(time).format('DD MMM YYYY') : time;
export const toDateTime = time => time ? moment(time).format('DD MMM YYYY hh:mm:ss a') : time;
export const todayStart = () => moment().startOf('day').format('YYYY-MM-DDTHH:mm')
export const todayEnd = () => moment().endOf('day').format('YYYY-MM-DDTHH:mm')
export const addDays = days => moment().add(days, 'days').endOf('day').format('YYYY-MM-DDTHH:mm')
export const round = (value, precision = 2) => parseFloat(new Decimal(value).toFixed(precision))
export const isNumber = number => number.match(/^\d+$/)
export const href = string => {
    const chars = { "-": "", " ": "-", "&": "and", "[": "", "]": "", "?": "", "#": "", "(": "", ")": "", "'": "" }
    for (let key in chars) {
        if (chars.hasOwnProperty(key)) {
            let value = chars[key]
            string = string.split(key).join(value)
        }
    }
    return string;
}
export const nonull = (json, exclude = []) => JSON.parse(JSON.stringify(json, (key, value) => value === null && exclude.indexOf(key) < 0 ? '' : value));
export const constructFormData = values => {
    const formData = new FormData()
    for (let field in values) {
        let value = values[field]
        formData.set(field, value)
    }
    return formData
}
export const inr = value => {
    if (!value || value === 0) return 0;
    let negative = false;
    if (value < 0) {
        negative = true;
        value *= -1;
    }
    let x = round(value, 2).toString();
    let split = x.split('.');
    let decimal = split[1] || null;
    x = split[0];
    let lastThree = x.substring(x.length - 3);
    let otherNumbers = x.substring(0, x.length - 3);
    if (otherNumbers !== '') {
        lastThree = ',' + lastThree;
    }
    let finalValue = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree + (decimal ? '.' + decimal : '');
    if (negative) {
        finalValue = "-" + finalValue
    }
    return finalValue
}