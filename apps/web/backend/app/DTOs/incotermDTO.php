<?php
  namespace App\DTOs;
  use App\DTOs\incotermTypeDTO;
  use App\DTOs\trackingStepDTO;

  readonly class IncotermDTO
  {
      public function __construct(
          public readonly incotermTypeDto $incotermType,
          public readonly TrackingStepDto $trackingStep,
      ) {
      }

      public static function fromModel(Incoterms $incoterm): self
      {
        if (!$incoterm) {
          return null;
        }
        return new self(
            incotermType: IncotermTypeDTO::fromModel($incoterm->incotermType),
            trackingStep: TrackingStepDTO::fromModel($incoterm->trackingStep),
        );
      }
  }
