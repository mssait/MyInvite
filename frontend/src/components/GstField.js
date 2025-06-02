import MaskedField from "./MaskedField";

const GstField = props => {
    return (
        <MaskedField definitions={{
            '#': /\d/,
            '$': /[A-Z]/,
            '0': /[0-9A-Z]/,
            'a': /Z/,
            '*': /[0-9A-Z]/,
        }} mask="##$$$$$####$0a*" {...props} />
    )
}
export default GstField