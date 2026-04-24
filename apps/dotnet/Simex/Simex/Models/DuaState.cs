using System;
using System.Collections.Generic;

namespace Simex.Models;

public partial class DuaState
{
    public int Id { get; set; }

    public string Name { get; set; } = null!;

    public string? Description { get; set; }

    public virtual ICollection<DuaStateHistory> DuaStateHistories { get; set; } = new List<DuaStateHistory>();

    public virtual ICollection<Dua> Duas { get; set; } = new List<Dua>();
}
