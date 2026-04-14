<?php
  namespace App\DTOs;
  use App\Models\IncotermTypes;

  readonly class IncotermTypeDTO
  {
      public function __construct(
          public readonly int $id,
          public readonly string $code,
          public readonly string $name,
      ) {
      }

      public static function fromModel(IncotermTypes $incotermType): self
      {
        if (!$incotermType) {
          return null;
        }

        return new self(
            id: $incotermType->ID,
            code: $incotermType->CODE,
            name: $incotermType->NAME,
        );
      }
  }
