<?php

namespace App\DTOs;
use App\Models\User;
use App\DTOs\roleDTO;
use App\DTOs\companyDTO;

readonly class UserDTO {
  public function __construct(
    public int $id,
    public string $email,
    public string $firstName,
    public string $lastName,
    public string $username,
    public bool $active,
    public ?string $identificationCardPath = null,
    public RoleDTO $role,
    public CompanyDTO $company,
  )
  {}

  public static function fromModel(?User $user): ?self {
    if (!$user) {
      return null;
    }

    return new self (
      id: $user->id,
      email: $user->email,
      firstName: $user->first_name,
      lastName: $user->last_name,
      username: $user->username,
      active: $user->active,
      identificationCardPath: $user->identification_card_path,
      role: RoleDTO::forModel($user->role),
      company: CompanyDTO::forModel($user->company)
    );
  }
}
