namespace Simex.Dtos
{
    public class OperationDto
    {
        public int Id { get; set; }

        public int OriginPortId { get; set; }

        public int DestinationPortId { get; set; }

        public decimal TotalCost { get; set; }

        public DateTime? Etd { get; set; }

        public DateTime? Eta { get; set; }

        public int? IncotermId { get; set; }

        public int? PiecesNumber { get; set; }

        public decimal Kilograms { get; set; }
    }
}
