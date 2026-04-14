<?php
  namespace App\DTOs;
  use App\DTOs\companyDTO;
  use App\DTOs\incotermDTO;
  use App\DTOs\userDTO;
  use App\DTOs\containerTypeDTO;
  use App\DTOs\packageTypeDTO;
  use App\DTOs\portDTO;

  use App\Models\Operations;

  readonly class OperationDTO
  {
    public function __construct(
      public int $id,
      public UserDTO $createUser,
      public string $orderReference,
      public CompanyDTO $importer,
      public string $pickUpData,
      public IncotermDTO $incoterm,
      public date $ETA,
      public CompanyDTO $exporter,
      public UserDTO $operationUser,
      public UserDTO $salesUser,
      public UserDTO $documentUser,
      public string $containerNumber,
      public ContainerTypeDTO $containerType,
      public string $hsCode,
      public int $packageNumber,
      public PackageTypeDTO $packageType,
      public PackageTypeDTO $packageSubtype,
      public float $volume,
      public float $netWeight,
      public int $piecesNuber,
      public string $mblNumber,
      public CompanyDTO $buyer,
      public CompanyDTO $seller,
      public CompanyDTO $naviera,
      public string $cargo,
      public PortDTO $originPort,
      public PortDTO $destinationPort,
      public string $cargoDescription,
      public float $total_cost,
      public float $total_sale,
      public float $profit
    ) {}

    public static function forModel (Operations $operation): self
    {
      if (!$operation) {
        return null;
      }

      return new self(
        id: $operation->ID,
        createUser: UserDTO::fromModel($operation->CREATE_USER),
        orderReference: $operation->ORDER_REFERENCE,
        importer: CompanyDTO::fromModel($operation->IMPORTER),
        pickUpData: $operation->PICK_UP_DATA,
        incoterm: IncotermDTO::fromModel($operation->INCOTERM),
        ETA: $operation->ETA,
        exporter: CompanyDTO::fromModel($operation->EXPORTER),
        operationUser: UserDTO::fromModel($operation->OPERATION_USER),
        salesUser: UserDTO::fromModel($operation->SALES_USER),
        documentUser: UserDTO::fromModel($operation->DOCUMENT_USER),
        containerNumber: $operation->CONTAINER_NUMBER,
        containerType: ContainerTypeDTO::fromModel($operation->CONTAINER_TYPE),
        hsCode: $operation->HS_CODE,
        packageNumber: $operation->PACKAGE_NUMBER,
        packageType: PackageTypeDTO::fromModel($operation->PACKAGE_TYPE),
        packageSubtype: PackageTypeDTO::fromModel($operation->PACKAGE_SUBTYPE),
        volume: $operation->VOLUME,
        netWeight: $operation->NET_WEIGHT,
        piecesNuber: $operation->PIECES_NUMBER,
        mblNumber: $operation->MBL_NUMBER,
        buyer: CompanyDTO::fromModel($operation->BUYER),
        seller: CompanyDTO::fromModel($operation->SELLER),
        naviera: CompanyDTO::fromModel($operation->NAVIERA),
        cargo: $operation->CARGO,
        originPort: PortDTO::fromModel($operation->ORIGIN_PORT),
        destinationPort: PortDTO::fromModel($operation->DESTINATION_PORT),
        cargoDescription: $operation->CARGO_DESCRIPTION,
        total_cost: $operation->TOTAL_COST,
        total_sale: $operation->TOTAL_SALE,
        profit: $operation->PROFIT
      );
    }

  }
