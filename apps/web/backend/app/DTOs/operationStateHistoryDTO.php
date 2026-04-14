<?php

namespace App\DTOs;
use App\Models\OperationStateHistory;
use App\DTOs\OperationStateDTO;
use App\DTOs\OperationDTO;

readonly class OperationStateHistoryDTO {
  public function __construct(
    public int $id,
    public \Carbon\Carbon|string $date,
    public OperationStateDTO $operationState,
    public OperationDTO $operation
  )
  {}

  public static function forModel(?\App\Models\OperationStateHistory $operationStateHistory): ?self {
    if (!$operationStateHistory) {
      return null;
    }

    return new self (
      id: $operationStateHistory->id,
      date: $operationStateHistory->date,
      operationState: OperationStateDTO::forModel($operationStateHistory->operationState),
      operation: OperationDTO::forModel($operationStateHistory->operation)
    );
  }
}
