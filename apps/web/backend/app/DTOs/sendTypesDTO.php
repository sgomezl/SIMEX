<?php

namespace App\DTOs;
use App\Models\SendTypes;

readonly class SendTypeDTO {
  public function __construct(
    public int $id,
    public string $name,
    public string $description
  )
  {}

  public static function forModel(?\App\Models\SendTypes $sendType): ?self {
    if (!$sendType) {
      return null;
    }

    return new self (
      id: $sendType->id,
      name: $sendType->name,
      description: $sendType->description
    );
  }
}
