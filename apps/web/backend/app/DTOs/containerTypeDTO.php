<?php

namespace App\DTOs;
use App\Models\ContainerTypes;

readonly class ContainerTypeDTO
{
  public function __construct(
    public int $id,
    public string $name,
    public ?string $description = null
  )
  {}

  public static function fromModel(ContainerTypes $containerType): self
  {
    if (!$containerType) {
      return null;
    }

    return new self(
      id: $containerType->ID,
      name: $containerType->NAME,
      description: $containerType->DESCRIPTION
    );
  }
}
