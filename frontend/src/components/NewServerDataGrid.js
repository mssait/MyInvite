import { Box, Chip, Link } from "@mui/material";
import { DataGridPremium, GridCellEditStopReasons, GridToolbar } from '@mui/x-data-grid-premium';
import { IconChevronDown, IconChevronUp } from "@tabler/icons-react";
import React, { useEffect, useState } from 'react';
import { Link as Href } from 'react-router-dom';
import fetcher from "../utils/fetcher";
import { toQueryString, useQuery } from "../utils/useQuery";
import { inr, toDate, toDateTime, toImage } from '../utils/util';

const NewServerDataGrid = ({
    ajax,
    headerFilters,
    customize,
    pageSizeOptions = [5, 10, 25, 50, 100, 250, 500],
    slotProps = {
        toolbar: {
            showQuickFilter: true
        }
    },
    refresh,
    Toolbar = GridToolbar,
    getDetailPanelContent,
    getDetailPanelHeight = () => 'auto',
    columns = [],
    aggregation,
    experimentalFeatures,
    processRowUpdate,
    onProcessRowUpdateError = console.log,
    checkboxSelection = false,
    onSelection,
    replace = true,
    url = true,
    rowModesModel,
    setRowModesModel,
    editMode,
    disableColumnSorting,
    disableColumnFilter,
    rowHeight = 52,
    apiRef
}) => {
    const defaultPageSize = 10
    let query = useQuery(replace);
    let state = useState('');
    let [params, setParams] = url ? query : state;
    const [selectionModel, setSelectionModel] = useState([]);

    const formatColumn = columns => {
        for (let i = 0; i < columns.length; i++) {
            if (!columns[i].type) {
                columns[i].type = 'string'
            }
            if (columns[i].type === 'url') {
                columns[i].renderCell = ({ value }) => <Link target="_blank" href={value}>{value}</Link>
                columns[i].type = 'string'
            } else if (columns[i].type === 'link') {
                columns[i].renderCell = ({ value, id }) => <Href to={columns[i].to({ id })}>{value}</Href>
                columns[i].type = 'string'
            } else if (columns[i].type === 'email') {
                columns[i].renderCell = ({ value }) => <Link href={`mailto:${value}`}>{value}</Link>
                columns[i].type = 'string'
            } else if (columns[i].type === 'phone') {
                columns[i].renderCell = ({ value }) => <Link href={`tel:+${value}`}>+{value}</Link>
                columns[i].type = 'string'
            } else if (columns[i].type === 'inr') {
                columns[i].valueFormatter = value => `₹${inr(value)}`
                columns[i].type = 'number'
            } else if (columns[i].type === 'dateTime') {
                columns[i].valueFormatter = value => toDateTime(value)
                columns[i].valueGetter = value => value && new Date(value)
            } else if (columns[i].type === 'date') {
                columns[i].valueFormatter = value => toDate(value)
                columns[i].valueGetter = value => value && new Date(value)
            } else if (columns[i].type === 'actions') {
                if (!columns[i].getActions) {
                    columns[i].getActions = () => []
                }
            } else if (columns[i].type === 'image') {
                columns[i].renderCell = ({ value }) => value && <img src={value} style={{ width: 'auto', height: '100%' }} />
                columns[i].valueGetter = value => value && toImage(value)
            } else if (columns[i].type === 'chip') {
                columns[i].type = 'string'
                columns[i].renderCell = ({ value }) => <Chip sx={{ color: columns[i]?.chipColor?.[value]?.[900], bgcolor: columns[i]?.chipColor?.[value]?.[100] }} label={value} />
            } else if (columns[i].type === 'chip-select') {
                columns[i].type = 'singleSelect'
                columns[i].renderCell = ({ formattedValue }) => <Chip sx={{ color: columns[i]?.chipColor?.[formattedValue]?.[900], bgcolor: columns[i]?.chipColor?.[formattedValue]?.[100] }} label={formattedValue} />
            }
        }
        return columns;
    }

    const getSortModel = () => (
        params.sortColumn ? [{
            field: params.sortColumn,
            sort: params.sortDirection,
        }] : [])

    const getFilterModel = () => {
        const items = []
        if (columns.length > 0 && params.filterColumn) {
            if (Array.isArray(params.filterColumn)) {
                for (let i = 0; i < params.filterColumn.length; i++) {
                    if (columns.map(({ field }) => field).indexOf(params.filterColumn[i]) > -1) {
                        items.push({
                            id: i,
                            field: params.filterColumn[i],
                            operator: params.filterOperator[i],
                            value: params.filterValue[i],
                        })
                    }
                }
            } else {
                items.push({
                    field: params.filterColumn,
                    operator: params.filterOperator,
                    value: params.filterValue
                })
            }
        }
        return {
            items,
            quickFilterValues: [params.search]
        }
    }

    const getPaginationModel = () => ({ page: (params.start || 0) / (params.length || defaultPageSize), pageSize: params.length || defaultPageSize })
    const initialState = {
        filter: {
            filterModel: getFilterModel()
        },
        sort: {
            sortModel: getSortModel()
        },
        pagination: {
            paginationModel: getPaginationModel()
        },
        aggregation
    }
    const pinnedColumns = {
        left: Array.isArray(params.pinLeft) ? params.pinLeft : [params.pinLeft],
        right: Array.isArray(params.pinRight) ? params.pinRight : [params.pinRight]
    }

    const setPinnedColumns = columns => {
        setParams({
            ...params,
            pinLeft: columns.left,
            pinRight: columns.right
        })
    }

    const formatQuickFilterValues = quickFilterValues => {
        if (quickFilterValues?.length === 0 ||
            quickFilterValues?.[0] === undefined ||
            quickFilterValues?.[0] === ''
        ) {
            return undefined
        }
        return quickFilterValues
    }

    const dataSource = React.useMemo(
        () => ({
            getRows: async ({ paginationModel, filterModel, sortModel }) => {
                const newParams = {
                    ...params,
                    filterColumn: (filterModel.items?.map(({ field }) => field)) || [],
                    filterOperator: (filterModel.items?.map(({ operator }) => operator)) || [],
                    filterValue: (filterModel.items?.map(({ value }) => value || '')) || [],
                    search: formatQuickFilterValues(filterModel.quickFilterValues),
                    sortColumn: sortModel[0]?.field || [],
                    sortDirection: sortModel[0]?.sort || [],
                    start: paginationModel.page * (params.length || defaultPageSize),
                    length: paginationModel.pageSize
                }
                setParams(newParams)
                const urlParams = new URLSearchParams({
                    paginationModel: JSON.stringify(paginationModel),
                    filterModel: JSON.stringify(filterModel),
                    sortModel: JSON.stringify(sortModel),
                });
                const res = await fetcher(`${ajax.url}?${toQueryString(newParams)}`)
                const json = await res.json()
                return {
                    rows: json.rows,
                    rowCount: json.rows.length,
                };
            },
        }),
        [],
    );

    const [loadingColumns, setLoadingColumns] = useState(true)
    useEffect(() => {
        const loadOptions = async () => {
            for (let i = 0; i < columns.length; i++) {
                if (columns[i].valueSelect) {
                    const res = await fetcher(`/api/select/${columns[i].valueSelect}`)
                    const json = await res.json()
                    columns[i].valueOptions = json.options
                    columns[i].getOptionValue = ({ id }) => id
                }
            }
            setLoadingColumns(false)
        }
        loadOptions()
    }, [])

    return (
        <Box width="100%">
            {loadingColumns ? (
                <DataGridPremium
                    disableDensitySelector
                    key={loadingColumns}
                    columns={formatColumn(columns)}
                    slots={{
                        toolbar: Toolbar,
                        detailPanelCollapseIcon: IconChevronUp,
                        detailPanelExpandIcon: IconChevronDown
                    }}
                    slotProps={{
                        toolbar: {
                            showQuickFilter: true
                        }
                    }}
                    loading={true}
                    sx={{
                        bgcolor: '#FFF',
                        '& .MuiDataGrid-detailPanel, [data-field=__detail_panel_toggle__]': {
                            overflow: 'visible',
                        }
                    }}
                />
            ) : (
                <DataGridPremium
                    apiRef={apiRef}
                    disableRowSelectionOnClick
                    disableDensitySelector
                    disableRowGrouping
                    disableChildrenSorting
                    disableColumnSorting={disableColumnSorting}
                    disableColumnFilter={disableColumnFilter}
                    rowHeight={rowHeight}
                    headerFilters={headerFilters}
                    sortingMode="server"
                    filterMode="server"
                    paginationMode="server"
                    selectionModel={selectionModel}
                    disableVirtualization
                    columns={formatColumn(columns)}
                    onRowSelectionModelChange={selected => {
                        onSelection && onSelection(selected)
                        setSelectionModel(selected)
                    }}
                    slots={{
                        toolbar: Toolbar,
                        detailPanelCollapseIcon: IconChevronUp,
                        detailPanelExpandIcon: IconChevronDown
                    }}
                    sx={{
                        bgcolor: '#FFF',
                        '& .MuiDataGrid-detailPanel, [data-field=__detail_panel_toggle__]': {
                            overflow: 'visible',
                        },
                    }}
                    slotProps={slotProps}
                    pinnedColumns={pinnedColumns}
                    onPinnedColumnsChange={setPinnedColumns}
                    getDetailPanelContent={getDetailPanelContent}
                    getDetailPanelHeight={getDetailPanelHeight}
                    experimentalFeatures={experimentalFeatures}
                    processRowUpdate={processRowUpdate}
                    onProcessRowUpdateError={onProcessRowUpdateError}
                    checkboxSelection={checkboxSelection}
                    editMode={editMode}
                    rowModesModel={rowModesModel}
                    onRowModesModelChange={rowModesModel => {
                        setRowModesModel && setRowModesModel(rowModesModel)
                    }}
                    onCellEditStop={({ reason }, { defaultMuiPrevented }) => {
                        if (reason === GridCellEditStopReasons.cellFocusOut) {
                            defaultMuiPrevented = true;
                        }
                    }}
                    initialState={initialState}
                    unstable_dataSource={dataSource}
                    pageSizeOptions={pageSizeOptions}
                    {...customize}
                />
            )}
        </Box>
    )
}
export default NewServerDataGrid;
