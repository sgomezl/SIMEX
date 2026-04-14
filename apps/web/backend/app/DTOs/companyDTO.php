<?php

namespace App\DTOs;
use App\Models\Company;
use App\DTOs\companyTypeDTO;
use App\DTOs\regionDTO;
use App\DTOs\cityDTO;


readonly class CompanyDTO {
  public function __construct(
    public int $id,
    public string $name,
    public CompanyTypeDTO $companyType,
    public string $iconPath,
    public string $email,
    public string $phoneNumber,
    public string $nif,
    public RegionDTO $region,
    public CityDTO $city,
    public string $address,
    public bool $active
  )
  {}

  public static function fromModel(?Company $company): ?self {
    if (!$company) {
      return null;
    }

    return new self (
      id: $company->id,
      name: $company->name,
      companyType: CompanyTypeDTO::fromModel($company->companyType),
      iconPath: $company->icon_path,
      email: $company->email,
      phoneNumber: $company->phone_number,
      nif: $company->nif,
      region: RegionDTO::fromModel($company->region),
      city: CityDTO::fromModel($company->city),
      address: $company->address,
      active: $company->active
    );
  }
}
