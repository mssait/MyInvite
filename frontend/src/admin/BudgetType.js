import { Box, Breadcrumbs, Button, Card, CardContent, Chip, Stack, Typography } from '@mui/material';
import { green, red } from '@mui/material/colors';
import {
  GridRowModes,
  GridToolbarContainer,
  GridToolbarExport,
  GridToolbarFilterButton,
  Toolbar
} from "@mui/x-data-grid-premium";
import { IconPlus } from '@tabler/icons-react';
import { useSnackbar } from "notistack";
import React, { useState } from 'react';
import ClientDataGrid from '../components/ClientDataGrid';
import fetcher from '../utils/fetcher';
import { constructFormData } from '../utils/util';

export const BudgetType = () => {
  const [rowModesModel, setRowModesModel] = useState({});
  const [appendRows, setAppendRows] = useState([]);
  const { enqueueSnackbar } = useSnackbar();
  const [refresh, setRefresh] = useState(0);

  const processRowUpdate = async (newRow, oldRow) => {
    const data = {
      budget_type: newRow.name,
    };
    if (!data.budget_type) {
      enqueueSnackbar("Budget type should not be empty", { variant: "warning" });
      setRowModesModel({
        ...rowModesModel,
        [oldRow.id]: {
          mode: GridRowModes.Edit,
          fieldToFocus: "name",
        },
      });
      return newRow;
    }
    let url, method, successMessage;

    if (typeof newRow.id === "number") {
      url = `/api/master/edit-budget-types/${newRow.id}`;
      method = "put";
      successMessage = "Edited successfully";
    } else if (newRow.id.indexOf("new-") === 0) {
      url = `/api/master/add-budget-types`;
      method = "post";
      successMessage = "Added successfully";
    }
    const res = await fetcher(url, {
      method,
      body: constructFormData(data),
    });
    const { status, message } = await res.json();
    if (status === "success") {
      enqueueSnackbar(successMessage, { variant: "success" });
      setRefresh((refresh) => refresh + 1);
      setAppendRows([]);
      return newRow;
    } else {
      enqueueSnackbar(message, { variant: "error" });
      return oldRow;
    }
  };

  const removeNewRow = (id) => {
    const newRows = [...appendRows];
    newRows.splice(
      newRows.findIndex((row) => row.id === id),
      1
    );
    setAppendRows(newRows);
  };


  const AddToolbar = ({ handleAdd }) => {
    return (
      <Toolbar disableGutters sx={{ px: 1 }}>
        <Button
          sx={{
            mt: 1,
            ml: 1,
            color: "white",
            backgroundColor: "primary.main",
            "&:hover": { backgroundColor: "primary.dark" },
          }}
          size="small"
          color="primary"
          startIcon={<IconPlus sx={{ color: "white" }} />}
          variant="contained"
          onClick={handleAdd}
        >
          Add budget types
        </Button>

        <Box sx={{ mt: 1, ml: 1 }}>
          <GridToolbarFilterButton />
        </Box>

        <Box sx={{ mt: 1, ml: 1 }}>
          <GridToolbarExport />
        </Box>
      </Toolbar>
    );
  };

  return (
    <Stack spacing={2}>
      <Card>
        <CardContent>
          <Box display={{ md: "flex", xs: "column" }} justifyContent="space-between" alignItems="center">
            <Box>
              <Typography variant="h3" pb={{ md: 0, xs: 1 }}>Budget Types</Typography>
            </Box>
            <Box sx={{ width: { xs: "80%", md: "auto" } }}>
              <Breadcrumbs separator="›">
                <Typography variant="h4" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                  Admin
                </Typography>
                <Typography variant="h4" color="text.primary" sx={{ fontSize: { xs: "0.75rem", md: "1.15rem" } }}>
                  Budget Types
                </Typography>
              </Breadcrumbs>
            </Box>
          </Box>
        </CardContent>
      </Card>
      <ClientDataGrid
        Toolbar={AddToolbar}
        slotProps={{
          toolbar: {
            handleAdd: () => {
              if (appendRows.length === 0) {
                const id = "new-" + new Date() * 1;
                const newRow = {
                  id,
                  name: "",
                };
                setAppendRows((appendRows) => [...appendRows, newRow]);
                setTimeout(() => {
                  setRowModesModel({
                    ...rowModesModel,
                    [id]: {
                      mode: GridRowModes.Edit,
                      fieldToFocus: "name",
                    },
                  });
                }, 0);
              }
            },
          },
        }}
        ajax={{ url: "/api/master/budget-types" }}
        columns={[
          {
            headerName: "Action",
            field: "Action",
            width: "150",
            id: "Action",
            type: "actions",
            getActions: ({ id }) => {
              const isInEditMode =
                rowModesModel[id]?.mode === GridRowModes.Edit;
              return isInEditMode
                ? [
                  <Chip
                    key="save"
                    label="Save"
                    variant="contained"
                    sx={{ bgcolor: green[100], color: green[900], borderRadius: "8px" }} onClick={() => {
                      setRowModesModel({
                        ...rowModesModel,
                        [id]: {
                          mode: GridRowModes.View,
                        },
                      });
                    }}
                  />,
                  <Chip
                    key="cancel"
                    label="Cancel"
                    variant="contained"
                    sx={{ bgcolor: red[100], color: red[900], borderRadius: "8px" }}
                    onClick={() => {
                      if (typeof id === "number") {
                        setRowModesModel({
                          ...rowModesModel,
                          [id]: {
                            mode: GridRowModes.View,
                            ignoreModifications: true,
                          },
                        });
                      } else if (id.indexOf("new-") === 0) {
                        removeNewRow(id);
                      }
                    }}
                  />,
                ]
                : [
                  <Chip
                    key="edit"
                    label="Edit"
                    variant="contained"
                    sx={{ bgcolor: green[100], color: green[900], borderRadius: "8px" }} onClick={() => {
                      setRowModesModel({
                        ...rowModesModel,
                        [id]: {
                          mode: GridRowModes.Edit,
                          fieldToFocus: "name",
                        },
                      });
                    }}
                  />
                ];
            },
          },
          {
            headerName: "Budget Type",
            field: "name",
            width: "200",
            id: "name",
            type: "string",
            editable: true,
          },

        ]}
        editMode="row"
        rowModesModel={rowModesModel}
        handleRowModesModel={setRowModesModel}
        processRowUpdate={processRowUpdate}
        appendRows={appendRows}
        refresh={refresh}
      />
    </Stack>
  )
}
