import { Avatar, Chip, ListItemButton, ListItemIcon, ListItemText, Typography, useMediaQuery } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { IconPoint } from '@tabler/icons-react';
import PropTypes from 'prop-types';
import { forwardRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getRoles, roleCheck } from "../../../../../auth/AuthProvider";
import config from '../../../../../config';


const NavItem = ({ item, level, isOpen, setIsOpen, setLeftDrawerOpened }) => {
    const theme = useTheme();
    const matchesSM = useMediaQuery(theme.breakpoints.down('md'));
    const roles = getRoles()
    const Icon = item.icon;

    const itemIcon = item?.icon ? (
        <Icon stroke={1.5} size="1.3rem" />
    ) : (
        <IconPoint
            sx={{
                width: isOpen === item?.id > -1 ? 8 : 6,
                height: isOpen === item?.id > -1 ? 8 : 6
            }}
            fontSize={level > 0 ? 'inherit' : 'medium'}
        />
    );

    let itemTarget = '_self';

    let listItemProps = item?.external ? {
        component: 'a', href: item.url, target: itemTarget
    } : {
        component: forwardRef((props, ref) => <Link ref={ref} {...props} to={item.url} target={itemTarget} />)
    }

    const itemHandler = id => {
        setIsOpen(id)
        if (matchesSM) {
            setLeftDrawerOpened(false)
        }
    };

    // active menu item on page load
    useEffect(() => {
        const currentIndex = document.location.pathname.toString().split('/').findIndex(id => id === item.id);
        if (currentIndex > -1) {
            setIsOpen(item.id)
        }
    }, []);

    return roleCheck(roles, item.roles) ? (
        <ListItemButton
            {...listItemProps}
            disabled={item.disabled}
            sx={{
                borderRadius: `${config.borderRadius}px`,
                mb: 0.5,
                alignItems: 'flex-start',
                backgroundColor: level > 1 ? 'transparent !important' : 'inherit',
                py: level > 1 ? 0.5 : 0.75,
                pl: `${level * 24}px`
            }}
            selected={isOpen === item.id}
            onClick={() => itemHandler(item.id)}
        >
            <ListItemIcon sx={{ my: 'auto', minWidth: !item?.icon ? 18 : 36 }}>{itemIcon}</ListItemIcon>
            <ListItemText
                primary={
                    <Typography variant={isOpen === item.id ? 'h5' : 'body1'} color="inherit">
                        {item.title}
                    </Typography>
                }
                secondary={
                    item.caption && (
                        <Typography variant="caption" sx={{
                            fontSize: '0.6875rem',
                            fontWeight: 500,
                            color: theme.palette.grey[500],
                            textTransform: 'capitalize'
                        }} display="block" gutterBottom>
                            {item.caption}
                        </Typography>
                    )
                }
            />
            {item.chip && (
                <Chip
                    color={item.chip.color}
                    variant={item.chip.variant}
                    size={item.chip.size}
                    label={item.chip.label}
                    avatar={item.chip.avatar && <Avatar>{item.chip.avatar}</Avatar>}
                />
            )}
        </ListItemButton>
    ) : null;
};

NavItem.propTypes = {
    item: PropTypes.object,
    level: PropTypes.number,
    setLeftDrawerOpened: PropTypes.func
};

export default NavItem;
