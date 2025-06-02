import { Box, Link } from "@mui/material";
import { DataGridPremium, GridCellEditStopReasons, GridToolbar, getGridNumericOperators, getGridSingleSelectOperators, getGridStringOperators } from '@mui/x-data-grid-premium';
import { IconChevronDown, IconChevronUp } from "@tabler/icons-react";
import React, { useEffect, useRef, useState } from 'react';
import fetcher from "../utils/fetcher";
import { toQueryString, useQuery } from "../utils/useQuery";
import { inr, toDate, toDateTime, toImage } from '../utils/util';
import Loader from "./Loader";

const ServerDataGrid = ({
    ajax,
    customize,
    client,
    rowsPerPageOptions = [5, 10, 25, 50, 100, 250, 500],
    slotProps,
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
    apiRef
}) => {
    const defaultPageSize = 10
    const [data, setData] = useState({
        rows: [],
        columns,
        rowCount: 0
    });
    let query = useQuery(replace);
    let state = useState('');
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


    const [sortModel, setSortModel] = useState(getSortModel());
    const [filterModel, setFilterModel] = useState(getFilterModel());
    const [paginationModel, setPaginationModel] = useState(getPaginationModel())

    const initialState = { filter: { filterModel }, sort: { sortModel }, pagination: { paginationModel }, aggregation }
    const controllerRef = useRef();

    const formatColumn = columns => {
        for (let i = 0; i < columns.length; i++) {
            if (!columns[i].type) {
                columns[i].type = 'string'
            }
            if (columns[i].type === 'url') {
                columns[i].renderCell = ({ value }) => <Link target="_blank" href={value}>{value}</Link>
                columns[i].type = 'string'
            } else if (columns[i].type === 'email') {
                columns[i].renderCell = ({ value }) => <Link href={`mailto:${value}`}>{value}</Link>
                columns[i].type = 'string'
            } else if (columns[i].type === 'phone') {
                columns[i].renderCell = ({ value }) => <Link href={`tel:+91${value}`}>{value}</Link>
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
                columns[i].renderCell = ({ value }) => value && <img src={value} style={{ width: 'auto', height: '100%' }} duration={0} />
                columns[i].valueGetter = value => value && toImage(value)
            }
            if (columns[i].type === 'string') {
                columns[i].filterOperators = getGridStringOperators().filter(({ value }) => value !== 'isAnyOf')
            }
            if (columns[i].type === 'singleSelect') {
                columns[i].filterOperators = getGridSingleSelectOperators().filter(({ value }) => value !== 'isAnyOf')
            }
            if (columns[i].type === 'number') {
                columns[i].valueFormatter = value => inr(value)
                columns[i].filterOperators = getGridNumericOperators().filter(({ value }) => value !== 'isAnyOf')
            }
        }
        return columns;
    }

    const loadOptions = async columns => {
        for (let i = 0; i < columns.length; i++) {
            if (columns[i].valueSelect) {
                const res = await fetcher(`/api/select/${columns[i].valueSelect}`)
                const json = await res.json()
                columns[i].valueOptions = json.options
                columns[i].getOptionValue = ({ id }) => id
            }
        }
        return columns;
    }

    useEffect(() => {
        setLoading(true)
        let url = client ? ajax.url : `${ajax.url}?${toQueryString(params)}`

        if (controllerRef.current) {
            controllerRef.current.abort();
        }

        const controller = new AbortController();
        controllerRef.current = controller;

        fetcher(url, {
            method: ajax.method || 'get',
            signal: controllerRef.current?.signal
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
            .catch(({ name }) => {
                if (name !== 'AbortError') {
                    setLoading(false)
                }
            })
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [params, refresh])

    const formatQuickFilterValues = quickFilterValues => {
        if (quickFilterValues?.length === 0 ||
            quickFilterValues?.[0] === undefined ||
            quickFilterValues?.[0] === ''
        ) {
            return undefined
        }
        return quickFilterValues
    }

    const filterChange = filterModel => {
        setParams({
            ...params, ...{
                filterColumn: (filterModel.items?.map(({ field }) => field)) || [],
                filterOperator: (filterModel.items?.map(({ operator }) => operator)) || [],
                filterValue: (filterModel.items?.map(({ value }) => value || '')) || [],
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
                <Loader />
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
                    initialState={initialState}
                    sortModel={sortModel}
                    filterModel={filterModel}
                    paginationModel={paginationModel}
                    selectionModel={selectionModel}
                    disableVirtualization
                    rowCount={(data.rowCount + appendRows.length) || 0}
                    filterMode={mode}
                    pagination={true}
                    paginationMode={mode}
                    sortingMode={mode}
                    columns={formatColumn(columns)}
                    rows={[...data.rows || [], ...appendRows]}
                    pageSizeOptions={rowsPerPageOptions}
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
                        },
                    }}
                    slotProps={slotProps}
                    loading={loading}
                    autoHeight={true}
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
export default ServerDataGrid;
