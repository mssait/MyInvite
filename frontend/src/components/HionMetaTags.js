
import { useEffect } from "react";
import config from '../config';

const HionMetaTags = ({ title = config.title, description = config.description, image = config.image }) => {

    useEffect(() => {
        document.querySelector('meta[name="title"]').content = title
        document.querySelector('meta[name="description"]').content = description


        document.querySelector('meta[property="og:title"]').content = title
        document.querySelector('meta[property="og:description"]').content = description


        document.title = title

        document.querySelector('meta[property="twitter:title"]').content = title
        document.querySelector('meta[property="twitter:description"]').content = description

    }, [title, description, image])

    return (
        <></>
    )

}
export default HionMetaTags