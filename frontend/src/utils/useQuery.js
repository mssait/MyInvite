import { useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { isNumber } from "./util";

export const useQuery = (replace = false) => {

    const { search, pathname } = useLocation();
    const navigate = useNavigate()
    const setParams = (params) => {
        navigate(`${pathname}?${toQueryString(params)}`, { replace })
    }

    return useMemo(() => {
        const result = {}
        for (let [key, value] of new URLSearchParams(search).entries()) {
            if (isNumber(value)) {
                value = parseInt(value)
            }
            if (result[key] !== undefined) {
                if (Array.isArray(result[key])) {
                    result[key].push(value)
                } else {
                    result[key] = [result[key], value]
                }
            } else {
                result[key] = value
            }
        }
        return [result, setParams]
    }, [search]);
}

export function toQueryString(params) {
    let query = ''
    for (let k in params) {
        if (params.hasOwnProperty(k)) {
            const v = params[k]
            if (v !== undefined) {
                const isArray = Array.isArray(v);
                if (!isArray || (isArray && v.length > 0)) {
                    query += '&' + (isArray ?
                        v.map(i => encodeURIComponent(k) + '=' + encodeURIComponent(i)).join('&') :
                        encodeURIComponent(k) + '=' + encodeURIComponent(v));
                }
            }
        }
    }
    return query.substring(1)
}