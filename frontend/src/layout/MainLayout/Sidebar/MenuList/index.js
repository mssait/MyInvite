import { Typography } from '@mui/material';
import NavGroup from './NavGroup';

const MenuList = ({
    menuItems,
    isOpen,
    setIsOpen,
    setLeftDrawerOpened
}) => menuItems.items.map(item => item.type === 'group' ? (
    <NavGroup setLeftDrawerOpened={setLeftDrawerOpened} isOpen={isOpen} setIsOpen={setIsOpen} key={item.id} item={item} />
) : (
    <Typography key={item.id} variant="h6" color="error" align="center">
        Menu Items Error
    </Typography>
));


export default MenuList;
