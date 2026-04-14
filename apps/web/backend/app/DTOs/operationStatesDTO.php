<?php

namespace App\DTOs;

readonly class CurrencyTypeDTO {
  public function __construct(
    public int $id,
    public string $name,
    public string $description
  )
  {}

  public static function forModel(?\App\Models\CurrencyType $currencyType): ?self {
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
