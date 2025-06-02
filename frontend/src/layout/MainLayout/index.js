import { AppBar, Box, Toolbar, useMediaQuery } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { useEffect, useState } from 'react';
import { Outlet } from 'react-router-dom';

import config from '../../config';
import Header from './Header';
import Sidebar from './Sidebar';

const MainLayout = ({ menuItems }) => {
    const theme = useTheme();
    const matchDownMd = useMediaQuery(theme.breakpoints.down('lg'));
    const [leftDrawerOpened, setLeftDrawerOpened] = useState(true);
    const handleLeftDrawerToggle = () => {
        setLeftDrawerOpened(leftDrawerOpened => !leftDrawerOpened)
    };

    useEffect(() => {
        setLeftDrawerOpened(!matchDownMd)
    }, [matchDownMd]);

    return (
        <Box sx={{ display: 'flex' }}>
            <AppBar
                enableColorOnDark
                position="fixed"
                color="inherit"
                elevation={0}
                sx={{
                    bgcolor: 'primary.main',
                    transition: leftDrawerOpened ? theme.transitions.create('width') : 'none'
                }}
            >
                <Toolbar>
                    <Header handleLeftDrawerToggle={handleLeftDrawerToggle} />
                </Toolbar>
            </AppBar>
            <Sidebar setLeftDrawerOpened={setLeftDrawerOpened} menuItems={menuItems} drawerOpen={leftDrawerOpened} drawerToggle={handleLeftDrawerToggle} />
            <Box sx={{
                backgroundColor: theme.palette.primary.light,
                width: '100%',
                minHeight: 'calc(100vh - 72px)',
                flexGrow: 1,
                padding: '16px',
                marginTop: '72px',
                marginRight: '8px',
                borderRadius: `${config.borderRadius}px`,
                ...(!leftDrawerOpened && {
                    borderBottomLeftRadius: 0,
                    borderBottomRightRadius: 0,
                    transition: theme.transitions.create('margin', {
                        easing: theme.transitions.easing.sharp,
                        duration: theme.transitions.duration.leavingScreen
                    }),
                    [theme.breakpoints.up('md')]: {
                        marginLeft: `-${config.drawerWidth - 8}px`,
                        width: `calc(100% - ${config.drawerWidth}px)`
                    },
                    [theme.breakpoints.down('md')]: {
                        marginLeft: '16px',
                        width: `calc(100% - ${config.drawerWidth}px)`,
                        padding: '16px'
                    },
                    [theme.breakpoints.down('sm')]: {
                        marginLeft: '10px',
                        width: `calc(100% - ${config.drawerWidth}px)`,
                        padding: '8px',
                        marginRight: '10px'
                    }
                }),
                ...(leftDrawerOpened && {
                    transition: theme.transitions.create('margin', {
                        easing: theme.transitions.easing.easeOut,
                        duration: theme.transitions.duration.enteringScreen
                    }),
                    marginLeft: 0,
                    borderBottomLeftRadius: 0,
                    borderBottomRightRadius: 0,
                    width: `calc(100% - ${config.drawerWidth}px)`,
                    [theme.breakpoints.down('md')]: {
                        marginLeft: '16px'
                    },
                    [theme.breakpoints.down('sm')]: {
                        marginLeft: '10px'
                    }
                })
            }}>
                <Outlet />
            </Box>
        </Box>
    );
};

export default MainLayout;
