import PropTypes from 'prop-types';

import { Divider, List, Typography } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import React from 'react';
import { getRoles, roleCheck } from "../../../../../auth/AuthProvider";
import NavCollapse from '../NavCollapse';
import NavItem from '../NavItem';

const NavGroup = ({ item, isOpen, setIsOpen, setLeftDrawerOpened }) => {
    const theme = useTheme();
    const roles = getRoles()
    // menu list collapse & items
    const items = item.children?.map((menu) => {
        if (roleCheck(roles, menu.roles)) {
            switch (menu.type) {
                case 'collapse':
                    return <NavCollapse setLeftDrawerOpened={setLeftDrawerOpened} isOpen={isOpen} setIsOpen={setIsOpen} key={menu.id} menu={menu} level={1} />;
                case 'item':
                    return <NavItem setLeftDrawerOpened={setLeftDrawerOpened} isOpen={isOpen} setIsOpen={setIsOpen} key={menu.id} item={menu} level={1} />;
                default:
                    return (
                        <Typography key={menu.id} variant="h6" color="error" align="center">
                            Menu Items Error
                        </Typography>
                    );
            }
        } else {
            return null
        }
    });

    return (
        roleCheck(roles, item.roles) ? (
            <React.Fragment>
                <List
                    subheader={
                        item.title && (
                            <Typography variant="caption" sx={{
                                fontSize: '0.875rem',
                                fontWeight: 500,
                                color: theme.palette.grey[900],
                                padding: '6px',
                                textTransform: 'capitalize',
                                marginTop: '10px'
                            }} display="block"
                                gutterBottom>
                                {item.title}
                                {item.caption && (
                                    <Typography variant="caption" sx={{
                                        fontSize: '0.6875rem',
                                        fontWeight: 500,
                                        color: theme.palette.grey[500],
                                        textTransform: 'capitalize'
                                    }} display="block"
                                        gutterBottom>
                                        {item.caption}
                                    </Typography>
                                )}
                            </Typography>
                        )
                    }
                >
                    {items}
                </List>

                {/* group divider */}
                <Divider sx={{ mt: 0.25, mb: 1.25 }} />
            </React.Fragment>
        ) : null
    );
};

NavGroup.propTypes = {
    item: PropTypes.object
};

export default NavGroup;
