param(
    [string]$BaseUrl = "http://127.0.0.1:8080/api/v1",
    [string]$Username = $env:COSTANORTE_DEMO_USER_USERNAME,
    [string]$Password = $env:COSTANORTE_DEMO_USER_PASSWORD
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "operador.demo"
}

if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "Costanorte2026!"
}

function Invoke-CostanorteApi {
    param(
        [Parameter(Mandatory = $true)]
        [ValidateSet("GET", "POST")]
        [string]$Method,
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [hashtable]$Headers,
        $Body
    )

    $invokeParams = @{
        Uri = "$BaseUrl/$Path"
        Method = $Method
    }

    if ($Headers) {
        $invokeParams.Headers = $Headers
    }

    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json"
        $invokeParams.Body = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }

    try {
        return Invoke-RestMethod @invokeParams
    }
    catch {
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader(
                $_.Exception.Response.GetResponseStream()
            )
            $message = $reader.ReadToEnd()
            throw "API $Method $Path falhou: $message"
        }
        throw
    }
}

function Get-WeekdayIndex {
    param([datetime]$Date)

    return (([int]$Date.DayOfWeek + 6) % 7)
}

function Get-WeekIndex {
    param(
        [datetime]$Date,
        [datetime]$StartDate
    )

    return [math]::Floor((New-TimeSpan -Start $StartDate -End $Date).Days / 7)
}

function Get-SlotKey {
    param(
        [datetime]$Date,
        [timespan]$StartTime,
        [timespan]$EndTime
    )

    return "{0}|{1}|{2}" -f $Date.ToString("yyyy-MM-dd"), $StartTime, $EndTime
}

function Test-Overlap {
    param(
        [hashtable]$OccupiedByDate,
        [datetime]$Date,
        [timespan]$StartTime,
        [timespan]$EndTime
    )

    $key = $Date.ToString("yyyy-MM-dd")
    if (-not $OccupiedByDate.ContainsKey($key)) {
        return $false
    }

    foreach ($slot in $OccupiedByDate[$key]) {
        if ($StartTime -lt $slot.End -and $EndTime -gt $slot.Start) {
            return $true
        }
    }

    return $false
}

function Add-OccupiedSlot {
    param(
        [hashtable]$OccupiedByDate,
        [datetime]$Date,
        [timespan]$StartTime,
        [timespan]$EndTime
    )

    $key = $Date.ToString("yyyy-MM-dd")
    if (-not $OccupiedByDate.ContainsKey($key)) {
        $OccupiedByDate[$key] = @()
    }

    $OccupiedByDate[$key] += [pscustomobject]@{
        Start = $StartTime
        End = $EndTime
    }
}

function New-BookingSpec {
    param(
        [datetime]$Date,
        [string]$StartTime,
        [string]$EndTime,
        [string]$CustomerType,
        [string]$CustomerName,
        [string]$CustomerReference,
        [int]$SlotSeed
    )

    $paid = $true
    $paymentMethod = $null
    $paymentDate = $null
    $paymentNotes = $null

    switch ($CustomerType) {
        "EXTERNAL" {
            $paid = (($Date.Day + $SlotSeed) % 4 -ne 0)
            if ($paid) {
                $methods = @("PIX", "CARD", "CASH", "TRANSFER")
                $paymentMethod = $methods[($Date.Day + $SlotSeed) % $methods.Length]
                $paymentDate = $Date.ToString("yyyy-MM-dd")
                switch ($paymentMethod) {
                    "PIX" { $paymentNotes = "Pago por pix na recepcao." }
                    "CARD" { $paymentNotes = "Pago com cartao no atendimento." }
                    "CASH" { $paymentNotes = "Pago em dinheiro." }
                    "TRANSFER" { $paymentNotes = "Transferencia confirmada." }
                }
            }
        }
        "PARTNER_COACH" {
            $paid = (($Date.Day + $SlotSeed) % 5 -ne 0)
            if ($paid) {
                $methods = @("PIX", "CARD")
                $paymentMethod = $methods[($Date.Day + $SlotSeed) % $methods.Length]
                $paymentDate = $Date.ToString("yyyy-MM-dd")
                $paymentNotes = "Pagamento registrado para aula de professor parceiro."
            }
        }
        default {
            $paid = $true
            $paymentMethod = "COURTESY"
            $paymentDate = $Date.ToString("yyyy-MM-dd")
            $paymentNotes = "Uso liberado pela operacao."
        }
    }

    return [pscustomobject]@{
        bookingDate = $Date.ToString("yyyy-MM-dd")
        startTime = "$StartTime`:00"
        endTime = "$EndTime`:00"
        customerName = $CustomerName
        customerReference = $CustomerReference
        customerType = $CustomerType
        paid = $paid
        paymentMethod = $paymentMethod
        paymentDate = $paymentDate
        paymentNotes = $paymentNotes
        materials = @()
    }
}

$login = Invoke-CostanorteApi -Method POST -Path "auth/login" -Body @{
    username = $Username
    password = $Password
}

$headers = @{
    Authorization = "$($login.tokenType) $($login.accessToken)"
}

$requiredPartnerCoaches = @(
    "Ana",
    "Diego",
    "Professor parceiro 3",
    "Marina Costa",
    "Rafael Mendes",
    "Bruna Castro"
)

$existingPartnerCoaches = @(
    Invoke-CostanorteApi -Method GET -Path "courts/partner-coaches?activeOnly=false" -Headers $headers
)
$existingPartnerCoachNames = $existingPartnerCoaches | ForEach-Object { $_.name }

foreach ($partnerCoachName in $requiredPartnerCoaches) {
    if ($existingPartnerCoachNames -contains $partnerCoachName) {
        continue
    }

    Invoke-CostanorteApi -Method POST -Path "courts/partner-coaches" -Headers $headers -Body @{
        name = $partnerCoachName
    } | Out-Null
}

$existingSummary = Invoke-CostanorteApi `
    -Method GET `
    -Path "courts/bookings/summary?dateFrom=2026-01-01&dateTo=2026-07-31" `
    -Headers $headers

if ($existingSummary.scheduledCount -ge 300 -and
    [decimal]$existingSummary.partnerCoachHours -ge 200 -and
    [decimal]$existingSummary.externalHours -ge 150) {
    [ordered]@{
        created = @{
            EXTERNAL = 0
            PARTNER_COACH = 0
            GUEST = 0
            VIP = 0
        }
        errors = 0
        summary = @{
            scheduledCount = $existingSummary.scheduledCount
            guestHours = $existingSummary.guestHours
            vipHours = $existingSummary.vipHours
            externalHours = $existingSummary.externalHours
            partnerCoachHours = $existingSummary.partnerCoachHours
        }
    } | ConvertTo-Json -Depth 6
    exit 0
}

$allBookings = @(
    Invoke-CostanorteApi -Method GET -Path "courts/bookings" -Headers $headers
)

$occupiedByDate = @{}
$occupiedSlotKeys = New-Object 'System.Collections.Generic.HashSet[string]'

foreach ($booking in $allBookings) {
    if ($booking.status -ne "SCHEDULED") {
        continue
    }

    $bookingDate = [datetime]$booking.bookingDate
    $bookingStart = [timespan]::Parse($booking.startTime)
    $bookingEnd = [timespan]::Parse($booking.endTime)

    Add-OccupiedSlot `
        -OccupiedByDate $occupiedByDate `
        -Date $bookingDate `
        -StartTime $bookingStart `
        -EndTime $bookingEnd

    $occupiedSlotKeys.Add(
        (Get-SlotKey -Date $bookingDate -StartTime $bookingStart -EndTime $bookingEnd)
    ) | Out-Null
}

$partnerCoachSeeds = @(
    @{ Name = "Ana"; Start = "08:00"; End = "09:30"; Weekday = 0; Reference = "Treino Ana" ; SlotSeed = 11 },
    @{ Name = "Diego"; Start = "08:30"; End = "10:00"; Weekday = 1; Reference = "Treino Diego"; SlotSeed = 12 },
    @{ Name = "Professor parceiro 3"; Start = "09:00"; End = "10:30"; Weekday = 2; Reference = "Treino parceiro"; SlotSeed = 13 },
    @{ Name = "Marina Costa"; Start = "08:00"; End = "09:30"; Weekday = 3; Reference = "Aula Marina"; SlotSeed = 14 },
    @{ Name = "Rafael Mendes"; Start = "09:00"; End = "10:30"; Weekday = 4; Reference = "Aula Rafael"; SlotSeed = 15 }
)

$externalSeeds = @(
    @{ Name = "Marcelo Costa"; Start = "10:00"; End = "11:00"; Weekday = 0; Reference = "Externo 01"; SlotSeed = 21 },
    @{ Name = "Juliana Alves"; Start = "18:00"; End = "19:00"; Weekday = 1; Reference = "Externo 02"; SlotSeed = 22 },
    @{ Name = "Bruno Silveira"; Start = "16:00"; End = "17:00"; Weekday = 2; Reference = "Externo 03"; SlotSeed = 23 },
    @{ Name = "Camila Ramos"; Start = "19:00"; End = "20:00"; Weekday = 3; Reference = "Externo 04"; SlotSeed = 24 },
    @{ Name = "Thiago Lima"; Start = "17:30"; End = "18:30"; Weekday = 4; Reference = "Externo 05"; SlotSeed = 25 }
)

$createdByType = @{
    PARTNER_COACH = 0
    EXTERNAL = 0
    VIP = 0
    GUEST = 0
}

$errors = New-Object 'System.Collections.Generic.List[string]'
$startDate = [datetime]"2026-01-01"
$endDate = [datetime]"2026-07-31"

for ($currentDate = $startDate; $currentDate -le $endDate; $currentDate = $currentDate.AddDays(1)) {
    $weekday = Get-WeekdayIndex -Date $currentDate
    $weekIndex = Get-WeekIndex -Date $currentDate -StartDate $startDate

    foreach ($seed in $partnerCoachSeeds) {
        if ($seed.Weekday -ne $weekday) {
            continue
        }

        $spec = New-BookingSpec `
            -Date $currentDate `
            -StartTime $seed.Start `
            -EndTime $seed.End `
            -CustomerType "PARTNER_COACH" `
            -CustomerName $seed.Name `
            -CustomerReference $seed.Reference `
            -SlotSeed $seed.SlotSeed

        $startTime = [timespan]::Parse($spec.startTime)
        $endTime = [timespan]::Parse($spec.endTime)
        $slotKey = Get-SlotKey -Date $currentDate -StartTime $startTime -EndTime $endTime
        if ($occupiedSlotKeys.Contains($slotKey) -or (Test-Overlap -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $startTime -EndTime $endTime)) {
            continue
        }

        try {
            Invoke-CostanorteApi -Method POST -Path "courts/bookings" -Headers $headers -Body $spec | Out-Null
            Add-OccupiedSlot -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $startTime -EndTime $endTime
            $occupiedSlotKeys.Add($slotKey) | Out-Null
            $createdByType.PARTNER_COACH++
        }
        catch {
            $errors.Add($_.Exception.Message)
        }
    }

    foreach ($seed in $externalSeeds) {
        if ($seed.Weekday -ne $weekday) {
            continue
        }

        $spec = New-BookingSpec `
            -Date $currentDate `
            -StartTime $seed.Start `
            -EndTime $seed.End `
            -CustomerType "EXTERNAL" `
            -CustomerName $seed.Name `
            -CustomerReference $seed.Reference `
            -SlotSeed $seed.SlotSeed

        $startTime = [timespan]::Parse($spec.startTime)
        $endTime = [timespan]::Parse($spec.endTime)
        $slotKey = Get-SlotKey -Date $currentDate -StartTime $startTime -EndTime $endTime
        if ($occupiedSlotKeys.Contains($slotKey) -or (Test-Overlap -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $startTime -EndTime $endTime)) {
            continue
        }

        try {
            Invoke-CostanorteApi -Method POST -Path "courts/bookings" -Headers $headers -Body $spec | Out-Null
            Add-OccupiedSlot -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $startTime -EndTime $endTime
            $occupiedSlotKeys.Add($slotKey) | Out-Null
            $createdByType.EXTERNAL++
        }
        catch {
            $errors.Add($_.Exception.Message)
        }
    }

    if ($weekday -eq 5 -and ($weekIndex % 2 -eq 0)) {
        $partnerWeekend = New-BookingSpec `
            -Date $currentDate `
            -StartTime "08:00" `
            -EndTime "09:30" `
            -CustomerType "PARTNER_COACH" `
            -CustomerName "Bruna Castro" `
            -CustomerReference "Clinica Bruna" `
            -SlotSeed 31

        $partnerWeekendStart = [timespan]::Parse($partnerWeekend.startTime)
        $partnerWeekendEnd = [timespan]::Parse($partnerWeekend.endTime)
        $slotKey = Get-SlotKey -Date $currentDate -StartTime $partnerWeekendStart -EndTime $partnerWeekendEnd
        if (-not $occupiedSlotKeys.Contains($slotKey) -and -not (Test-Overlap -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $partnerWeekendStart -EndTime $partnerWeekendEnd)) {
            try {
                Invoke-CostanorteApi -Method POST -Path "courts/bookings" -Headers $headers -Body $partnerWeekend | Out-Null
                Add-OccupiedSlot -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $partnerWeekendStart -EndTime $partnerWeekendEnd
                $occupiedSlotKeys.Add($slotKey) | Out-Null
                $createdByType.PARTNER_COACH++
            }
            catch {
                $errors.Add($_.Exception.Message)
            }
        }
    }

    if ($weekday -eq 5 -and ($weekIndex % 4 -eq 0)) {
        $vipSpec = New-BookingSpec `
            -Date $currentDate `
            -StartTime "10:00" `
            -EndTime "11:00" `
            -CustomerType "VIP" `
            -CustomerName "Fernanda Rocha" `
            -CustomerReference "Apto 301" `
            -SlotSeed 41

        $vipStart = [timespan]::Parse($vipSpec.startTime)
        $vipEnd = [timespan]::Parse($vipSpec.endTime)
        $slotKey = Get-SlotKey -Date $currentDate -StartTime $vipStart -EndTime $vipEnd
        if (-not $occupiedSlotKeys.Contains($slotKey) -and -not (Test-Overlap -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $vipStart -EndTime $vipEnd)) {
            try {
                Invoke-CostanorteApi -Method POST -Path "courts/bookings" -Headers $headers -Body $vipSpec | Out-Null
                Add-OccupiedSlot -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $vipStart -EndTime $vipEnd
                $occupiedSlotKeys.Add($slotKey) | Out-Null
                $createdByType.VIP++
            }
            catch {
                $errors.Add($_.Exception.Message)
            }
        }
    }

    if ($weekday -eq 6 -and ($weekIndex % 5 -eq 0)) {
        $guestSpec = New-BookingSpec `
            -Date $currentDate `
            -StartTime "09:00" `
            -EndTime "10:00" `
            -CustomerType "GUEST" `
            -CustomerName "Helena Duarte" `
            -CustomerReference "Apto 118" `
            -SlotSeed 51

        $guestStart = [timespan]::Parse($guestSpec.startTime)
        $guestEnd = [timespan]::Parse($guestSpec.endTime)
        $slotKey = Get-SlotKey -Date $currentDate -StartTime $guestStart -EndTime $guestEnd
        if (-not $occupiedSlotKeys.Contains($slotKey) -and -not (Test-Overlap -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $guestStart -EndTime $guestEnd)) {
            try {
                Invoke-CostanorteApi -Method POST -Path "courts/bookings" -Headers $headers -Body $guestSpec | Out-Null
                Add-OccupiedSlot -OccupiedByDate $occupiedByDate -Date $currentDate -StartTime $guestStart -EndTime $guestEnd
                $occupiedSlotKeys.Add($slotKey) | Out-Null
                $createdByType.GUEST++
            }
            catch {
                $errors.Add($_.Exception.Message)
            }
        }
    }
}

$summary = Invoke-CostanorteApi `
    -Method GET `
    -Path "courts/bookings/summary?dateFrom=2026-01-01&dateTo=2026-07-31" `
    -Headers $headers

[ordered]@{
    created = $createdByType
    errors = $errors.Count
    summary = @{
        scheduledCount = $summary.scheduledCount
        guestHours = $summary.guestHours
        vipHours = $summary.vipHours
        externalHours = $summary.externalHours
        partnerCoachHours = $summary.partnerCoachHours
    }
} | ConvertTo-Json -Depth 6
