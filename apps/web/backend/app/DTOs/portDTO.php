<?php

namespace App\DTOs;
use App\Models\Ports;
use App\DTOs\CityDTO;

readonly class PortDTO {
  public function __construct(
    public int $id,
    public string $name,
    public CityDTO $city,
  )
  {}

  public static function fromModel(Ports $port): self
  {
    if (!$port) {
      return null;
    }

    return new self(
      id: $port->ID,
      name: $port->NAME,
      city: $port->CITY
    );
  }
}
