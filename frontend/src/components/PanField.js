import MaskedField from "./MaskedField";

const PanField = props => {
    return (
        <MaskedField definitions={{
            '#': /\d/,
            '$': /[A-Z]/
        }} mask="$$$$$####$" {...props} />
    )
}
export default PanField