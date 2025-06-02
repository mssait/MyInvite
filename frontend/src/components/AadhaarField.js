import MaskedField from "./MaskedField";

const AadhaarField = props => {
    return (
        <MaskedField definitions={{
            '#': /\d/,
            '$': /[2-9]/
        }} mask="$### #### ####" {...props} />
    )
}
export default AadhaarField