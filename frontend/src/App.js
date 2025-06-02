import { CssBaseline, StyledEngineProvider } from '@mui/material';
import { ThemeProvider } from '@mui/material/styles';
import { LicenseInfo } from '@mui/x-license';
import './global.css';
import NavigationScroll from './layout/NavigationScroll';
import Routes from './routes';
import theme from './themes/theme';
LicenseInfo.setLicenseKey('e62622550a8ec71dafc2ed1559066863Tz0xLEU9MzMwMjA1MTM4MTY0NCxTPXByZW1pdW0sTE09c3Vic2NyaXB0aW9uLEtWPTI=');

const App = () => {
  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <NavigationScroll>
          <Routes />
        </NavigationScroll>
      </ThemeProvider>
    </StyledEngineProvider >
  );
};

export default App;
