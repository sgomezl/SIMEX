<?php

namespace App\DTOs;
use App\DTOs\countryDTO;
use App\DTOs\regionDTO;
use App\Models\Cities;

readonly class CityDTO {
  public function __construct(
    public int $id,
    public string $name,
    public CountryDTO $country,
    public RegionDTO $region,
    public ?int $latitude,
    public ?int $longitude,
    public ?int $altitude
  )
  {}

  public static function fromModel($city): self
  {
    if (!$city) {
      return null;
    }

    return new self(
      id: $city->ID,
      name: $city->NAME,
      country: $city->COUNTRY,
      region: $city->REGION,
      latitude: $city->LATITUDE,
      longitude: $city->LONGITUDE,
      altitude: $city->ALTITUDE
    );
  }
}
