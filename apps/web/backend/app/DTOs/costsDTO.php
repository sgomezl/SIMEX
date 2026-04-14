<?php

namespace App\DTOs;
use App\DTOs\OperationDTO;
use App\DTOs\CostTypeDTO;
use App\DTOs\CurrencyTypeDTO;

readonly class CostDTO {
  public function __construct(
    public int $id,
    public OperationDTO $operation,
    public CostTypeDTO $costType,
    public CurrencyTypeDTO $currencyType,
    public float $cost,
    public float $costAmount,
    public string $sale,
    public string $saleAmount
  )
  {}

  public static function forModel(CostTypes $costType): ?self {
    if (!$costType) {
      return null;
    }

    return new self (
      id: $costType->id,
      operation: OperationDTO::forModel($costType->operation),
      costType: CostTypeDTO::forModel($costType->costType),
      currencyType: CurrencyTypeDTO::forModel($costType->currencyType),
      cost: $costType->cost,
      costAmount: $costType->cost_amount,
      sale: $costType->sale,
      saleAmount: $costType->sale_amount
    );
  }
}
