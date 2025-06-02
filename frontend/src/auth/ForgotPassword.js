import { LoadingButton } from "@mui/lab";
import {
    Box,
    Button,
    Card,
    CardContent,
    Container,
    FormControl,
    FormHelperText,
    Grid,
    Stack,
    TextField,
    Typography
} from "@mui/material";
import { Formik } from "formik";
import { Link, Navigate, useNavigate } from "react-router-dom";
import * as Yup from "yup";
import LogoSection from "../layout/MainLayout/LogoSection";
import fetcher from "../utils/fetcher";
import { constructFormData } from "../utils/util";
import { getHomePage, isLoggedIn } from "./AuthProvider";

export default function ForgotPassword() {
    const navigate = useNavigate()

    return isLoggedIn() ? (
        <Navigate to={getHomePage()} />
    ) : (
        <Box bgcolor="primary.light" height="100vh" display="flex" alignItems="center">
            <Container maxWidth="sm">
                <Card elevation={2}>
                    <CardContent>
                        <Stack spacing={2} textAlign="center" justifyContent="center">
                            <LogoSection />
                            <Formik
                                validationSchema={Yup.object().shape({
                                    username: Yup.string().max(100).required('Enter Email')
                                })}
                                onSubmit={async (values, { setSubmitting }) => {
                                    setSubmitting(true)
                                    return await fetcher('/forgot-password', {
                                        method: 'post',
                                        body: constructFormData(values)
                                    })
                                        .then(r => r.json())
                                        .then(({ status, message = "Try again" }) => {
                                            if (status === "success") {

                                                navigate("/login")
                                            } else {

                                            }
                                        })
                                        .catch(() => {

                                        })
                                }}
                                initialValues={{
                                    username: ''
                                }}>
                                {({
                                    errors,
                                    handleBlur,
                                    handleChange,
                                    handleSubmit,
                                    isSubmitting,
                                    touched,
                                    values
                                }) => (
                                    <form noValidate onSubmit={handleSubmit}>
                                        <Grid container>
                                            <Grid item xs={12}>
                                                <FormControl fullWidth
                                                    error={Boolean(touched.username && errors.username)}>
                                                    <TextField
                                                        required
                                                        id="username"
                                                        type="email"
                                                        value={values.username}
                                                        name="username"
                                                        onBlur={handleBlur}
                                                        onChange={handleChange}
                                                        label="Email Id"
                                                    />
                                                    {touched.username && errors.username && (
                                                        <FormHelperText error id="error-username">
                                                            {errors.username}
                                                        </FormHelperText>
                                                    )}
                                                </FormControl>
                                            </Grid>
                                        </Grid>
                                        {errors.submit && (
                                            <Box mt={3}>
                                                <FormHelperText id='error-submit'
                                                    error>{errors.submit}</FormHelperText>
                                            </Box>
                                        )}
                                        <Box mt={2}>
                                            <LoadingButton
                                                loading={isSubmitting}
                                                fullWidth
                                                size="large"
                                                type="submit"
                                                variant="contained"
                                            >
                                                Submit
                                            </LoadingButton>
                                        </Box>
                                    </form>
                                )}
                            </Formik>
                            <Typography>
                                Know you password? <Button component={Link} to="/login" textAlign="center">
                                    Login
                                </Button>
                            </Typography>
                        </Stack>
                    </CardContent>
                </Card>
            </Container>
        </Box>
    )
}
