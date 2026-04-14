<?php

namespace App\DTOs;
use App\DTOs\countryDTO;
use App\Models\Region;

readonly class RegionDTO {
  public function __construct(
    public int $id,
    public string $name,
    public CountryDTO $country
  )
  {}

  public static function fromModel(Region $region): self
  {
    if (!$region) {
      return null;
    }

    return new self(
      id: $region->ID,
      name: $region->NAME,
      country: CountryDTO::fromModel($region->COUNTRY)
    );
  }
}
