import { Avatar, Box, Chip, Link } from "@mui/material";
import { DataGridPremium, GridCellEditStopReasons, GridToolbar } from '@mui/x-data-grid-premium';
import { IconChevronDown, IconChevronUp } from "@tabler/icons-react";
import React, { useEffect, useState } from 'react';
import { Link as Href } from 'react-router-dom';
import fetcher from "../utils/fetcher";
import { toQueryString, useQuery } from "../utils/useQuery";
import { inr, toDate, toDateTime, toImage } from '../utils/util';

const ClientDataGrid = ({
    ajax,
    customize,
    client = true,
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
    appendRows = [],
    disableColumnSorting,
    disableColumnFilter,
    rowHeight = 52,
    headerFilters,
    apiRef
}) => {
    const defaultPageSize = 10
    const [data, setData] = useState({
        rows: [],
        columns,
        rowCount: client ? undefined : 0
    });
    let query = useQuery(replace);
    let state = useState({});
    let [params, setParams] = url ? query : state;
    const [rendering, setRendering] = useState(true)
    const [loading, setLoading] = useState(false);
    const [selectionModel, setSelectionModel] = useState([]);

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
                    let index = columns.map(({ field }) => field).indexOf(params.filterColumn[i])
                    if (index > -1) {
                        items.push({
                            id: i,
                            field: params.filterColumn[i],
                            operator: params.filterOperator[i],
                            value: ['dateTime', 'date', 'singleSelect'].indexOf(columns[index].type) > -1 ? params.filterValue[i] : String(params.filterValue[i]),
                        })
                    }
                }
            } else {
                let index = columns.map(({ field }) => field).indexOf(params.filterColumn)
                items.push({
                    field: params.filterColumn,
                    operator: params.filterOperator,
                    value: ['dateTime', 'date', 'singleSelect'].indexOf(columns[index].type) > -1 ? params.filterValue : String(params.filterValue),
                })
            }
        }
        return {
            items,
            quickFilterValues: [String(params?.search || '')]
        }
    }

    const getPaginationModel = () => ({ page: (params.start || 0) / (params.length || defaultPageSize), pageSize: params.length || defaultPageSize })


    const [sortModel, setSortModel] = useState(getSortModel());
    const [filterModel, setFilterModel] = useState(getFilterModel());
    const [paginationModel, setPaginationModel] = useState(getPaginationModel())

    const initialState = { filter: { filterModel }, sort: { sortModel }, pagination: { paginationModel }, aggregation }

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
            else if (columns[i].type === 'avatar') {
                columns[i].renderCell = ({ value }) => value && <Avatar src={value} style={{ width: 'auto', height: '100%' }} />
                columns[i].valueGetter = value => value && toImage(value)
            }
        }
        return columns;
    }

    const loadOptions = async columns => {
        for (let i = 0; i < columns.length; i++) {
            if (columns[i].valueSelect) {
                const res = await fetcher(`/api/select/${columns[i].valueSelect}`)
                const { options } = await res.json()
                columns[i].valueOptions = options
                columns[i].getOptionValue = ({ id }) => id
            }
        }
        return columns;
    }

    useEffect(() => {
        setLoading(true)
        let url = client ? ajax.url : `${ajax.url}?${toQueryString(params)}`

        fetcher(url, {
            method: ajax.method || 'get'
        })
            .then(res => res.json())
            .then(async data => {
                columns = await loadOptions(columns)
                setData(data)
                setSortModel(getSortModel())
                setFilterModel(getFilterModel())
                setPaginationModel(getPaginationModel());
                setRendering(false)
                setLoading(false)
            })
            .catch(() => {
                setLoading(false)
            })
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [refresh])

    useEffect(() => {
        setSortModel(getSortModel())
        setFilterModel(getFilterModel())
        setPaginationModel(getPaginationModel());
    }, [params])

    const formatQuickFilterValues = quickFilterValues => {
        if (quickFilterValues?.length === 0 ||
            quickFilterValues?.[0] === undefined ||
            quickFilterValues?.[0] === ''
        ) {
            return undefined
        }
        return quickFilterValues.join(' ')
    }

    const filterChange = filterModel => {
        setParams({
            ...params, ...{
                filterColumn: (filterModel.items?.map(({ field }) => field)) || [],
                filterOperator: (filterModel.items?.map(({ operator }) => operator)) || [],
                filterValue: (filterModel.items?.map(({ value }) => value instanceof Date ? value * 1 : (value || ''))) || [],
                search: formatQuickFilterValues(filterModel.quickFilterValues)
            }
        })
    };

    const sortChange = data => {
        setParams({
            ...params,
            sortColumn: data[0]?.field || [],
            sortDirection: data[0]?.sort || []
        })
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

    const setPage = ({ page, pageSize }) => {
        setParams({ ...params, start: page * (params.length || defaultPageSize), length: pageSize })
    }

    const mode = client ? 'client' : 'server'

    return (
        <Box width="100%">
            {rendering ? (
                <DataGridPremium
                    disableDensitySelector
                    key={rendering}
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
                    headerFilters={headerFilters}
                    apiRef={apiRef}
                    disableRowSelectionOnClick
                    disableDensitySelector
                    disableRowGrouping
                    disableChildrenSorting
                    disableColumnSorting={disableColumnSorting}
                    disableColumnFilter={disableColumnFilter}
                    rowHeight={rowHeight}
                    initialState={initialState}
                    sortModel={sortModel}
                    filterModel={filterModel}
                    paginationModel={paginationModel}
                    selectionModel={selectionModel}
                    disableVirtualization
                    rowCount={client ? undefined : (data.rowCount + appendRows.length) || 0}
                    filterMode={mode}
                    pagination={true}
                    paginationMode={mode}
                    sortingMode={mode}
                    columns={formatColumn(columns)}
                    rows={[...data.rows || [], ...appendRows]}
                    pageSizeOptions={pageSizeOptions}
                    onPaginationModelChange={setPage}
                    onFilterModelChange={filterChange}
                    onSortModelChange={sortChange}
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
                        }
                    }}
                    slotProps={slotProps}
                    loading={loading}
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
                    {...customize}
                />
            )}
        </Box>
    )
}
export default ClientDataGrid;