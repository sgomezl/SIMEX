<?php
  namespace App\DTOs;
  use App\Models\TrackingSteps;

  readonly class TrackingStepDTO
  {
      public function __construct(
          public readonly int $id,
          public readonly string $orderNum,
          public readonly string $name,
      ) {
      }

      public static function fromModel(TrackingSteps $trackingStep): self
      {

        if (!$trackingStep) {
          return null;
        }

        return new self(
            id: $trackingStep->ID,
            orderNum: $trackingStep->ORDER_NUM,
            name: $trackingStep->NAME,
        );
      }
  }
