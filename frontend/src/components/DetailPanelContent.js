import { Box } from '@mui/material';
import { useGridApiContext } from '@mui/x-data-grid-premium';
import React from 'react'

export default function DetailPanelContent({ children }) {
    const apiRef = useGridApiContext();
    const [width, setWidth] = React.useState(() => {
        const dimensions = apiRef.current.getRootDimensions();
        return dimensions.viewportInnerSize.width;
    });

    const handleViewportInnerSizeChange = React.useCallback(() => {
        const dimensions = apiRef.current.getRootDimensions();
        setWidth(dimensions.viewportInnerSize.width);
    }, [apiRef]);

    React.useEffect(() => {
        return apiRef.current.subscribeEvent(
            'viewportInnerSizeChange',
            handleViewportInnerSizeChange,
        );
    }, [apiRef, handleViewportInnerSizeChange]);
    return (
        <Box position="sticky" left={0} width={width}>
            {children}
        </Box>
    )
}
