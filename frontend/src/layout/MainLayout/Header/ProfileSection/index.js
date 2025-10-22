import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';

import {
    Avatar,
    ButtonBase,
    Divider,
    ListItemIcon,
    ListItemText,
    Menu,
    MenuItem,
    Typography
} from '@mui/material';

import { IconLogout } from '@tabler/icons-react';
import { getAvatar, getEmail, getName } from '../../../../auth/AuthProvider';
import { toImage } from '../../../../utils/util';

const ProfileSection = () => {
    const [open, setOpen] = useState(false);
    const anchorRef = useRef(null);

    const handleClose = ({ target }) => {
        if (anchorRef.current && anchorRef.current.contains(target)) {
            return;
        }
        setOpen(false);
    };

    const handleToggle = () => {
        setOpen(prevOpen => !prevOpen);
    };

    const prevOpen = useRef(open);
    useEffect(() => {
        if (prevOpen.current === true && open === false) {
            anchorRef.current.focus();
        }
        prevOpen.current = open;
    }, [open]);

    return (
        <React.Fragment>
            {getAvatar() ? (
                <ButtonBase
                    onClick={handleToggle}
                    ref={anchorRef}
                    aria-controls={open ? 'menu-list-grow' : undefined}
                    aria-haspopup="true"
                    sx={{ borderRadius: '50%' }}
                >
                    <Avatar
                        src={toImage(getAvatar())}
                        sx={{
                            width: 56,
                            height: 56,
                        }}
                    />
                </ButtonBase>
            ) : (
                <ButtonBase
                    onClick={handleToggle}
                    ref={anchorRef}
                    aria-controls={open ? 'menu-list-grow' : undefined}
                    aria-haspopup="true"
                    sx={{ borderRadius: '50%' }}
                >
                    <Avatar
                        sx={{
                            width: 50,
                            height: 50,
                        }}
                    />
                </ButtonBase>
            )}

            <Menu
                placement="bottom-end"
                open={open}
                anchorEl={anchorRef.current}
                onClose={handleClose}
                PaperProps={{
                    elevation: 0,
                    sx: {
                        minWidth: 200,
                        overflow: 'visible',
                        filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
                        mt: 1.5,
                        '& .MuiAvatar-root': {
                            width: 32,
                            height: 32,
                            ml: -0.5,
                            mr: 1,
                        },
                        '&:before': {
                            content: '""',
                            display: 'block',
                            position: 'absolute',
                            top: 0,
                            right: 14,
                            width: 10,
                            height: 10,
                            bgcolor: 'background.paper',
                            transform: 'translateY(-50%) rotate(45deg)',
                            zIndex: 0,
                        },
                    },
                }}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
            >
                <Typography px={2} pt={1} pb={2}>Name: {getName()}</Typography>
                <Typography px={2} pb={2}>Email:  {getEmail()}</Typography>
                <Divider />
                <MenuItem component={Link} to="/logout">
                    <ListItemIcon>
                        <IconLogout />
                    </ListItemIcon>
                    <ListItemText primary={<Typography variant="body2">Logout</Typography>} />
                </MenuItem>
            </Menu>
        </React.Fragment>
    );
};

export default ProfileSection;
