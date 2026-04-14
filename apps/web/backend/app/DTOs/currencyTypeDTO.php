<?php

namespace App\DTOs;
use App\Models\CurrencyTypes;

readonly class CurrencyTypeDTO {
  public function __construct(
    public int $id,
    public string $name,
    public string $description
  ) {}

  public static function forModel(CurrencyTypes $currencyType): ?self {
    if (!$currencyType) {
      return null;
    }

    return new self (
      id: $currencyType->id,
      name: $currencyType->name,
      description: $currencyType->description
    );
  }
}
