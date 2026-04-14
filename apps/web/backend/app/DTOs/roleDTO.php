<?php

namespace App\DTOs;
use App\Models\Role;

readonly class RoleDTO {
  public function __construct(
    public int $id,
    public string $name,
    public ?string $description = null,
  )
  {}

  public static function fromModel(Role $role): self
  {
    if (!$role) {
      return null;
    }

    return new self(
      id: $role->ID,
      name: $role->NAME,
      description: $role->DESCRIPTION
    );
  }
}
