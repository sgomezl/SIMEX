using System;
using System.Collections.Generic;

namespace Simex.Models;

public partial class DuaStateHistory
{
    public int Id { get; set; }

    public int DuaId { get; set; }

    public int DuaStateId { get; set; }

    public DateTime ChangeDate { get; set; }

    public int UserId { get; set; }

    public string? Notes { get; set; }

    public virtual Dua Dua { get; set; } = null!;

    public virtual DuaState DuaState { get; set; } = null!;

    public virtual User User { get; set; } = null!;
}
