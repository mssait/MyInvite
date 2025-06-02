import ArrowForwardIosRoundedIcon from '@mui/icons-material/ArrowForwardIosRounded';
import { Typography } from '@mui/material';
import MuiAccordion from '@mui/material/Accordion';
import MuiAccordionDetails from '@mui/material/AccordionDetails';
import MuiAccordionSummary from '@mui/material/AccordionSummary';
import { styled } from '@mui/material/styles';
import { useState } from 'react';

const Accordion = styled(props => (
    <MuiAccordion disableGutters elevation={0} square {...props} />
))(({ theme }) => ({
    marginBottom: theme.spacing(2),
    '&:not(:last-child)': {
        borderBottom: 0,
    },
    '&:before': {
        display: 'none',
    },
}));

const AccordionSummary = styled(props => (
    <MuiAccordionSummary
        expandIcon={
            <ArrowForwardIosRoundedIcon />
        }
        {...props}
    />
))(({ theme }) => ({
    padding: theme.spacing(2),
    backgroundColor: '#f9f9f9',
    flexDirection: 'row',
    '& .MuiAccordionSummary-expandIconWrapper.Mui-expanded': {
        transform: 'rotate(90deg)',
    }
}));

const AccordionDetails = styled(MuiAccordionDetails)(({ theme }) => ({
    padding: theme.spacing(2),
}));

export default function Faq({ faqs }) {
    const [expanded, setExpanded] = useState(0);

    const handleChange = panel => (_, newExpanded) => {
        setExpanded(newExpanded ? panel : false);
    };

    return (
        faqs.map(({ question, answer }, index) => (
            <Accordion key={index} expanded={expanded === index} onChange={handleChange(index)}>
                <AccordionSummary aria-controls={`${index}-content`} id={`${index}-header`}>
                    <Typography mr={1} variant="h5" color="brand">{question}</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Typography variant="body2" textAlign="justify" dangerouslySetInnerHTML={{
                        __html: answer
                    }} />
                </AccordionDetails>
            </Accordion>
        ))
    )
}
