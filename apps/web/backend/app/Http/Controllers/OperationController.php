<?php

namespace App\Http\Controllers;

use App\DTOs\OperationDTO;
use App\Models\Costs;
use App\Models\Operations;
use App\Models\OperationStateHistory;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;
use Illuminate\Validation\Rule;

class OperationController extends Controller
{
  public function store(Request $request)
  {
    $operationValidated = $request->validate([
      'CREATE_USER_ID' => 'required|exists:users,id',
      'ORDER_REFERENCE' => 'required|string|max:255',
      'IMPORTER_ID' => [
        'required',
        Rule::exists('company', 'id')->whereIn('COMPANY_TYPE_ID', [1, 3]),
      ],
      'PICKUP_DATA' => 'required|string',
      'INCORTERM_ID' => 'required|exists:incoterms,id',
      'ETD' => 'required|date',
      'CUSTOMS_AGENT_ID' => 'required|exists:users,id,ROLE_ID,5',
      'ETA' => 'required|date',
      'EXPORTATOR_ID' => [
        'required',
        Rule::exists('company', 'id')->whereIn('COMPANY_TYPE_ID', [2, 3]),
      ],
      'OPERATION_USER_ID' => 'required|exists:users,id',
      'SALES_USER_ID' => 'required|exists:users,id',
      'DOCUMENT_USER_ID' => 'required|exists:users,id',
      'CONTAINER_NUMBER' => 'required|string|max:255',
      'CONTAINER_TYPE_ID' => 'required|exists:container_types,id',
      'HS_CODE' => 'required|string|max:255',
      'PACKAGES_NUMBER' => 'required|integer',
      'PACKAGE_TYPE_ID' => 'required|exists:package_types,id',
      'PACKAGE_SUB_TYPE_ID' => 'required|exists:package_types,id',
      'VOLUME' => 'required|numeric',
      'NET_WEIGHT' => 'required|numeric',
      'KILOGRAMS' => 'required|numeric',
      'PIECES_NUMBER' => 'required|integer',
      'MBL_NUMBER' => 'required|string|max:255',
      'BUYER_ID' => [
        'required',
        Rule::exists('company', 'id')->whereIn('COMPANY_TYPE_ID', [1, 2, 3]),
      ],
      'SELLER_ID' => [
        'required',
        Rule::exists('company', 'id')->whereIn('COMPANY_TYPE_ID', [1, 2, 3]),
      ],
      'NAVIERA_ID' => 'required|exists:company,id,COMPANY_TYPE_ID,4',
      'CARGO' => 'required|string|max:255',
      'ORIGIN_PORT_ID' => 'required|exists:ports,id',
      'DESTINATION_PORT_ID' => 'required|exists:ports,id',
      'CARGO_DESCRIPTION' => 'required|string',
      'TOTAL_COST' => 'required|numeric',
      'TOTAL_SALE' => 'required|numeric',
      'PROFIT' => 'required|numeric',

      'COSTS' => 'nullable|array',
      'COSTS.*.COST_TYPE_ID'     => 'required|exists:cost_types,id',
      'COSTS.*.CURRENCY_TYPE_ID' => 'required|exists:currency_types,id',
      'COSTS.*.SENED_TYPE_ID'    => 'required|exists:send_types,id',
      'COSTS.*.COST'             => 'required|numeric',
      'COSTS.*.COST_AMOUNT'      => 'required|integer',
      'COSTS.*.SALE'             => 'required|numeric',
      'COSTS.*.SALE_AMOUNT'      => 'required|integer',
    ]);

    DB::beginTransaction();

    try {
      $costsArray = $operationValidated['COSTS'] ?? [];
      unset($operationValidated['COSTS']);

      $operation = Operations::create($operationValidated);

      OperationStateHistory::create([
        'OPERATION_ID' => $operation->ID,
        'OPERATION_STATE_ID' => 1,
        'DATE' => Carbon::now(),
      ]);

      if (!empty($costsArray)) {
        $costsData = array_map(function ($cost) use ($operation) {
          return [
            'OPERATION_ID' => $operation->ID,
            'COST_TYPE_ID' => $cost['COST_TYPE_ID'],
            'CURRENCY_TYPE_ID' => $cost['CURRENCY_TYPE_ID'],
            'SENED_TYPE_ID' => $cost['SENED_TYPE_ID'],
            'COST' => $cost['COST'],
            'COST_AMOUNT' => $cost['COST_AMOUNT'],
            'SALE' => $cost['SALE'],
            'SALE_AMOUNT' => $cost['SALE_AMOUNT'],
          ];
        }, $costsArray);

        Costs::insert($costsData);
      }

      $operation->load([
        'createUser', 'importer', 'incoterm', 'exportator',
        'operationUser', 'salesUser', 'documentUser',
        'containerType', 'packageType', 'packageSubType',
        'buyer', 'seller', 'naviera', 'originPort', 'destinationPort'
      ]);

      DB::commit();

      return response()->json([
        'message' => 'Operation created successfully',
        'operation' => OperationDTO::forModel($operation)
      ], 201);
    } catch (\Exception $e) {
      DB::rollback();
      return response()->json([
        'message' => 'Failed to create operation',
        'error' => $e->getMessage()
      ], 500);
    }
  }
}
