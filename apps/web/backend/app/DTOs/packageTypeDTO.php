<?php

namespace App\DTOs;
use App\Models\PackageType;

readonly class PackageTypeDTO {
  public function __construct(
    public int $id,
    public string $name,
    public ?string $description = null,
    public ?string $parentId = null,
  )
  {}

  public static function fromModel(PackageType $packageType): self
  {
    if (!$packageType) {
      return null;
    }

    return new self(
      id: $packageType->ID,
      name: $packageType->NAME,
      description: $packageType->DESCRIPTION,
      parentId: $packageType->PARENT_ID
    );
  }
}
