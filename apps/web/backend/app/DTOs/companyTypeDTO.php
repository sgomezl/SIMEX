<?php

namespace App\DTOs;
use App\Models\CompanyType;

readonly class companyTypeDTO {
  public function __construct(
    public int $id,
    public string $name,
    public ?string $description = null
  )
  {}

  public static function fromModel(?CompanyType $companyType): self
  {
    if (!$companyType) {
      return null;
    }
    return new self(
      id: $companyType->ID,
      name: $companyType->NAME,
      description: $companyType->DESCRIPTION
    );
  }
}
